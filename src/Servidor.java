import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Servidor {

	private List<HiloCliente> listaClientes = new ArrayList<HiloCliente>();
	
	public static void main(String[] args) {
		new Servidor();
	}
	
	public Servidor(){
		try {
			ServerSocket serverSocket = new ServerSocket(6001);
			while(true){
				Socket socket = serverSocket.accept();
				System.out.println("se ha conectado un cliente");
				this.listaClientes.add(new HiloCliente(socket, this));
			}
		 }catch (IOException e) {
			e.printStackTrace();
		 }
	}
	
	public void enviarATodos(HiloCliente hiloCliente, String mensaje){
		for(HiloCliente hc : this.listaClientes ){
			if (hc != hiloCliente){
				hc.enviar(mensaje);
			}
		}
	}
}