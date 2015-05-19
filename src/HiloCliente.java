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
	private String usuario;

	public HiloCliente(Socket socketCliente, Servidor server) {
		this.servidor = server;
		this.socketCliente = socketCliente;

		try {
			this.br = new BufferedReader(new InputStreamReader(this.socketCliente.getInputStream()));
			this.socketCliente.setSoTimeout(60 * 1000);

			this.bw = new BufferedWriter(new OutputStreamWriter(this.socketCliente.getOutputStream()));
			if(this.logarUsuario()){
				new Thread(this).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			String mensaje = "Se ha conectado: " + this.usuario;
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
			
				this.servidor.eliminarUsuarioLogado(this.usuario);
				try {
					this.socketCliente.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.closed = true;
				this.servidor.eliminarListaHilos(this);;
			
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
	
	//	Metodo para logar usuario y devolver si se ha logado o no
	public boolean logarUsuario(){
		boolean logado = false;
		try {
			for(int i = 0; i < 3; i++){
				this.escribir("Introduzca el usuario");
				this.usuario = this.br.readLine();
				
				this.escribir("Introduzca la contraseña");
				String pass = this.br.readLine();
				
				if (!this.servidor.comprobarClienteLogado(this.usuario)) {
					if (new RegistroUsuarios().verificarUsuario(this.usuario, pass)) {
						this.escribir("El usuario y la contraseña son válidas");
						this.servidor.añadirUsuairoLogado(this.usuario);
						logado = true;
						break;
					} else {
						this.escribir("Nombre de usuario y/o contraseña erroneos");	
					}
	
				} else {
					this.escribir("El usuairo ya está logado");
					return logado;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logado;
	}
	
}
