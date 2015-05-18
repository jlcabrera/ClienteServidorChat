import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sun.swing.internal.plaf.synth.resources.synth;


public class Servidor {

	private List<HiloCliente> listaClientes = new ArrayList<HiloCliente>();
	private List<String> clientesLogados = new LinkedList<String>();
	
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
	
	public synchronized void enviarATodos(HiloCliente hiloCliente, String mensaje){
		for(HiloCliente hc : this.listaClientes ){
			if (hc != hiloCliente){
				hc.enviar(mensaje);
			}
		}
	}
	
	public synchronized void añadirUsuairoLogado(String usuario){
		this.clientesLogados.add(usuario);
	}
	
	public synchronized void eliminarUsuarioLogado(String usuario){
		this.clientesLogados.remove(usuario);
	}
	
	public synchronized boolean comprobarClienteLogado(String usuario){
		for (String string : clientesLogados) {
			if(string.equals(usuario)){
				return true;
			}
		}
		return false;
	}
}