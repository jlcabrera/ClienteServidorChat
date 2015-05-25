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
			this.socketCliente.setSoTimeout(10 * 60 * 1000);

			this.bw = new BufferedWriter(new OutputStreamWriter(
					this.socketCliente.getOutputStream()));
			this.escribir(CodigosError.CONEXION_REALIZADA.getCodigo() + " " + CodigosError.CONEXION_REALIZADA.getMensaje());
			new Thread(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String mensaje;
		try {
			while (!closed) {
				
				mensaje = br.readLine();
				detectarComando(mensaje);
				
			}
		} catch (SocketTimeoutException e) {
			this.escribir("Se te ha desconectado del servidor por inactividad");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(this.user != null){
				this.servidor.eliminarUsuarioLogado(this.user.getNick());
				this.servidor.eliminarListaHilos(this, this.user.getNick());
			}
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
	public boolean logarUsuario(String[] credenciales){
		boolean logado = false;
		try {
			String userName = validarUsuarioYPass(credenciales[1], credenciales[2]);
			if (!userName.equals("")) {
				this.escribir(CodigosError.USUARIO_CORRECTO.getCodigo() + " " + CodigosError.USUARIO_CORRECTO.getMensaje());
				for(int i = 0; i < 3; i ++){
					escribir("Introduzca el nick que desee");
					String nick = this.br.readLine();
					
					if(this.servidor.comprobarNick(nick)){
						this.user = this.servidor.addUsuarioLogado(userName, nick );
						this.servidor.enviarATodos(this, "El usuario " + this.user.getNick() + " se ha conectado");
						escribir("Te has conectado al servidor");
						return true;
					}else{
						escribir("El nick que ha seleccionado no esta disponible");
					}
				}
			} else {
				this.escribir(CodigosError.USUARIO_ERRONEO.getCodigo() + " " + CodigosError.USUARIO_ERRONEO.getMensaje());
				return false;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}  catch(NullPointerException e){
			System.err.println(CodigosError.USUARIO_ERRONEO.getCodigo() + " Ha introducido mal las credenciales, cuando se realiza el login se tiene que escribir el usuario y la pass en la misma linea");
			return  false;
		}catch(ArrayIndexOutOfBoundsException e){
			System.err.println(CodigosError.USUARIO_ERRONEO.getCodigo() + " Ha introducido solo el nombre de usuario");
			return false;
		}
		
		return logado;
	}

	public String validarUsuarioYPass(String user, String contra){

		String usuario = user;
		String pass = contra;
		
		if (!this.servidor.comprobarClienteLogado(usuario) && new RegistroUsuarios().verificarUsuario(usuario, pass)) {
			return usuario;
		} else {
			
			return "";
		}		
	}

	public Usuario getUsuario(){
		return this.user;
	}

	// Metodo para cambiar el nick del usuario
	public void cambiarNick(String nick){
	
		String nickAntiguo = this.user.getNick();
		String nickNuevo = nick;
		
		if(this.servidor.comprobarNick(nickNuevo)){
			this.gestor.setNick(nickAntiguo, nickNuevo);
			this.servidor.enviarATodos(this, "*** el usuario " + nickAntiguo + " es ahora conocido por " + nickNuevo);
			System.out.println("*** el usuario " + nickAntiguo + " es ahora conocido por " + nickNuevo);
			this.user.setNick(nickNuevo);
		}else{
			this.escribir("El nick que has escrito no está disponible");
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
				String [] credenciales = mensaje.split(" ");
				logarUsuario(credenciales);
			}else if(mensaje.contains("/nick")){
				String nick = "";
				for(int i = 6; i < mensaje.length(); i++){
					nick = nick + mensaje.charAt(i);
				}
				cambiarNick(nick);
			}else if(mensaje.contains("/msg")){
				String nick = "";
				int indice = 5;
				for(; indice < mensaje.length() && mensaje.charAt(indice) != ' '; indice ++){
					nick = nick + mensaje.charAt(indice);
				}
				String texto = "";
				for(indice ++; indice < mensaje.length(); indice++ ){
					texto = texto + mensaje.charAt(indice);
				}
				mandarMensajePrivado(nick, this.user.getNick() + " te ha escrito: " + texto);
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
			if(this.user != null){
				this.servidor.enviarATodos(this, mensaje);
				System.out.println(this.user.getNick() +": "+mensaje);
			}else{
				escribir("El usuario no se ha logado en el sistema");
			}
		}
	}
}
