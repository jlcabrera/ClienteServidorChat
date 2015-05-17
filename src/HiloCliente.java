import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class HiloCliente implements Runnable {

	private Servidor servidor;
	private Socket socketCliente;
	private BufferedReader br;
	private BufferedWriter bw;
	private boolean closed = false;

	public HiloCliente(Socket socketCliente, Servidor server) {
		this.servidor = server;
		this.socketCliente = socketCliente;

		try {
			
			this.br = new BufferedReader(new InputStreamReader(this.socketCliente.getInputStream()));
			
			this.bw = new BufferedWriter(new OutputStreamWriter(this.socketCliente.getOutputStream()));
			
			this.bw.write("Introduzca el nombre de usuairo");
			this.bw.newLine();
			this.bw.flush();
			String usuario = this.br.readLine();
			
			this.bw.write("Introduzca la contraeña");
			this.bw.newLine();
			this.bw.flush();

			String pass = this.br.readLine();
			
			if(!this.servidor.comprobarClienteLogado(usuario)){
				this.servidor.añadirUsuairoLogado(usuario);
				
				if (new RegistroUsuarios().verificarUsuario(usuario, pass)) {
					this.bw.write("El usuario y la contraseña son validos");
					this.bw.flush();
					new Thread(this).start();
				} else {
					this.bw.write("Nombre y/o usuario incorrecto");
					this.bw.flush();
				}
				
			}else{
				this.bw.write("El usuario ya está logado");
				this.bw.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	

	@Override
	public void run() {
		while (!closed) {
			try {
				String mensaje = br.readLine();
				System.out.println(mensaje);
				this.servidor.enviarATodos(HiloCliente.this, mensaje);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// metodo para enviar el mensaje
	public void enviar(String text) {
		try {
			this.bw.write(text);
			this.bw.newLine();
			this.bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
