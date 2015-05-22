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
	private Usuario u;
	private GestorUsuarios gu;

	public HiloCliente(Socket socketCliente, Servidor server) {
		this.servidor = server;
		this.socketCliente = socketCliente;
		this.gu = this.servidor.getGestorUsuario();
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
				mensaje = br.readLine();
				System.out.println(mensaje);
				this.servidor.enviarATodos(HiloCliente.this, mensaje);
			}
		} catch (SocketTimeoutException e) {
			this.escribir("Se te ha desconectado del servidor por inactividad");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
			this.servidor.eliminarNick(this.u.getNick());
			this.servidor.eliminarUsuarioLogado(this.u);
			this.servidor.eliminarListaHilos(this, this.u.getNick());
			
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
						this.u = this.gu.addUsuario(userName, nick);
						this.servidor.addNick(nick);
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

	public void solicitarNick() {
		this.escribir("Introduzca el nick que desea usar");
	}
	
	public Usuario getUsuario(){
		return this.u;
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
				usuario = "";
				return usuario;
			}catch(NullPointerException e){
				System.err.println("Ha introducido mal las credenciales, cuando se realiza el login se tiene que escribir el usuario y la pass en la misma linea");
				usuario = "";
			}catch(ArrayIndexOutOfBoundsException e){
				System.err.println("Ha introducido solo el nombre de usuario");
				usuario = "";
			}
		}
		
		return usuario;
	}

	// Metodo para cambiar el nick del usuario
	public void cambiarNick(){
		escribir("Introduce el nuevo nick: ");
		try {
			String nickAntiguo = this.u.getNick();
			String nickNuevo = this.br.readLine();
			
			if(this.servidor.comprobarNick(nickNuevo)){
				this.gu.setNick(nickAntiguo, nickNuevo);
				this.servidor.enviarATodos(this, "*** el usuario " + nickAntiguo + " es ahora conocido por " + nickNuevo);
				this.u.setNick(nickNuevo);
			}else{
				this.escribir("El nick que has escrito no está disponible");
				this.socketCliente.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void mandarMensajePrivado(String nick, String mensaje){
		this.servidor.enviarAUsuario(nick, mensaje);
	}
}
