import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HiloCliente implements Runnable {

	private Servidor servidor;
	private Socket socketCliente;
	private BufferedReader br;
	private BufferedWriter bw;
	private boolean closed = false;
	private Usuario user;
	private GestorUsuarios gestor;
	
	public HiloCliente(Socket socketCliente, Servidor server) {
		this.servidor = server;
		this.socketCliente = socketCliente;
		this.gestor = this.servidor.getGestorUsuario();
		try {
			this.br = new BufferedReader(new InputStreamReader(
					this.socketCliente.getInputStream()));
			this.socketCliente.setSoTimeout(60 * 1000);

			this.bw = new BufferedWriter(new OutputStreamWriter(
					this.socketCliente.getOutputStream()));
			if (this.logarUsuario()) {
				new Thread(this).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String mensaje;
		try {
			while (!closed) {
				
				//Modificar la logica de esto para que no se vaya de madre
				mensaje = br.readLine();
				detectarComando(mensaje);
				
			}
		} catch (SocketTimeoutException e) {
			this.escribir("Se te ha desconectado del servidor por inactividad");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
			this.servidor.eliminarUsuarioLogado(this.user.getNick());
			this.servidor.eliminarListaHilos(this, this.user.getNick());
			
			if(this.br != null){
				try {
					this.br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(this.bw != null){
				try {
					this.bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(this.socketCliente != null){
				try {
					this.socketCliente.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.closed = true;
		}
	}

	// Metodo para logar usuario y devolver si se ha logado o no
	public boolean logarUsuario(){
		boolean logado = false;
		try {
			String userName = solicitarUsuarioYPass();
			if (!userName.equals("")) {
				for(int i = 0; i < 3; i ++){
					escribir("Introduzca el nick que desee");
					String nick = this.br.readLine();
					
					if(this.servidor.comprobarNick(nick)){
						this.user = this.servidor.addUsuarioLogado(userName, nick );
						return true;
					}else{
						escribir("El nick que ha seleccionado no esta disponible");
					}
				}
			} else {
				this.escribir("El usuario y contraseña son erroneos o el usuario ya está logado");
				return false;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}  
		return logado;
	}

	public String solicitarUsuarioYPass(){
		String usuario = "";
		for(int i = 0; i < 3; i++){
			
			try {
				String [] credenciales = this.br.readLine().split(" ");
				usuario = credenciales[0];
				String pass = credenciales[1];
				
				if (!this.servidor.comprobarClienteLogado(usuario) && new RegistroUsuarios().verificarUsuario(usuario, pass)) {
					this.escribir("El usuario y la contraseña son válidas");
					return usuario;
				} else {
					this.escribir("El usuario y contraseña son erroneos o el usuario ya está logado");
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				return "";
			}catch(NullPointerException e){
				System.err.println("Ha introducido mal las credenciales, cuando se realiza el login se tiene que escribir el usuario y la pass en la misma linea");
				return  "";
			}catch(ArrayIndexOutOfBoundsException e){
				System.err.println("Ha introducido solo el nombre de usuario");
				return "";
			}
		}
		
		return usuario;
	}

	public void solicitarNick() {
		this.escribir("Introduzca el nick que desea usar");
	}

	public Usuario getUsuario(){
		return this.user;
	}

	// Metodo para cambiar el nick del usuario
	public void cambiarNick(){
		escribir("Introduce el nuevo nick: ");
		try {
			String nickAntiguo = this.user.getNick();
			String nickNuevo = this.br.readLine();
			
			if(this.servidor.comprobarNick(nickNuevo)){
				this.gestor.setNick(nickAntiguo, nickNuevo);
				this.servidor.enviarATodos(this, "*** el usuario " + nickAntiguo + " es ahora conocido por " + nickNuevo);
				this.user.setNick(nickNuevo);
			}else{
				this.escribir("El nick que has escrito no está disponible");
				this.socketCliente.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(this.socketCliente != null){
				try {
					this.socketCliente.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Metodo para escribir en el socket
	public void escribir(String mensaje) {
		try {
			this.bw.write(mensaje);
			this.bw.newLine();
			this.bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mandarMensajePrivado(String nick, String mensaje){
		if(!this.servidor.enviarAUsuario(nick, mensaje)){
			escribir("El usuario no se ha encontrado");
		}
	}
	
	//Metodo para leer el mensaje y saber si contiene algun comando o no
	public void detectarComando(String mensaje){
		if(mensaje.contains("/")){
			if(mensaje.contains("/login")){
				logarUsuario();
			}else if(mensaje.contains("/nick")){
				cambiarNick();
			}else if(mensaje.contains("/msg")){
				String nick = "";
				int indice = 5;
				for(; indice < mensaje.length() || mensaje.charAt(indice) == ' '; indice ++){
					nick = nick + mensaje.charAt(indice);
				}
				String texto = "";
				for(indice ++; indice < mensaje.length(); indice++ ){
					texto = texto + mensaje.charAt(indice);
				}
				mandarMensajePrivado(nick, texto);
			}else if(mensaje.contains("/quit")){
				this.closed = true;
			}else if(mensaje.contains("/userlist")){
				String lista = "";
				for(String nick: this.servidor.listarUsuarios()){
					lista = lista + " " + nick;
				}
				
				escribir(lista);
			}else if(mensaje.contains("/?")){
				escribir("los comandos validos son:\n"
						+ "/loggin para logarse en el servidor \n"
						+ "/nick para cambiar el nick \n"
						+ "/msg \"usuario\" para enviar un mensaje a un solo destinatario \n"
						+ "/userlist para obtener una lista de los nicks de los clientes que estan ahora conectados \n"
						+ "/quit para salir desconectar \n"
						+ "si no escribe ninguno de estos comandos se enviara el mensaje por la sala general");
				
			}else{
				escribir("El comando escrito no es válido, escriba: \"/?\" para saber los comandos disponibles");
			}
		}else{
			this.servidor.enviarATodos(this, mensaje);
			System.out.println(mensaje);
			
		}
	}
}
