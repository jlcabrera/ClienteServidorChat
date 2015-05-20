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

	public HiloCliente(Socket socketCliente, Servidor server) {
		this.servidor = server;
		this.socketCliente = socketCliente;

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

		try {
			String mensaje = "Se ha conectado: " + this.u.getNick();
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

			this.servidor.eliminarUsuarioLogado(this.u);
			try {
				this.socketCliente.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.closed = true;
			this.servidor.eliminarListaHilos(this);
			;

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
				escribir("Introduzca el nick que desee");
				String nick = this.br.readLine();
				
				if(this.servidor.comprobarNick(nick)){
					
					Usuario user = new Usuario(userName, nick);
					this.servidor.añadirUsuairoLogado(user);
					logado = true;
				}else{
					escribir("El nick que ha seleccionado no esta disponible");
				}
			} else {
				this.escribir("El usuario y contraseña son erroneos o el usuario ya está logado");
				return logado;
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
	
	public String solicitarUsuarioYPass(){
		boolean verificarDatos = false;
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
				return usuario;
			}catch(NullPointerException e){
				System.err.println("Ha introducido mal las credenciales, cuando se realiza el login se tiene que escribir el usuario y la pass en la misma linea");
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
				this.servidor.enviarATodos(this, "*** el usuario " + nickAntiguo + " es ahora conocido por " + nickNuevo);
				this.u.setNick(nickNuevo);
			}else{
				this.escribir("El nick que has escrito no está disponible");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
