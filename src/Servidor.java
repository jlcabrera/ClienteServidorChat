import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Servidor {

	private List<HiloCliente> listaClientes = new ArrayList<HiloCliente>();
	private List<Usuario> clientesLogados = new ArrayList<Usuario>();
	private List<String> nicksRegistrados = new ArrayList<String>();
	private final String [] PALABRAS_RESERVADAS = { "nick", "msg", "login", "quit", "userlist", "ping"};
	public static void main(String[] args) {
		new Servidor();
	}
	
	public Servidor(){
		try {
			ServerSocket serverSocket = new ServerSocket(6001);
			while(true){
				Socket socket = serverSocket.accept();
				this.listaClientes.add(new HiloCliente(socket, this));
			}
		 }catch (IOException e) {
			e.printStackTrace();
		 }
	}
	public synchronized void enviarATodos(HiloCliente hiloCliente, String mensaje){
		for(HiloCliente hc : this.listaClientes ){
			if (hc != hiloCliente){
				hc.escribir(mensaje);
			}
		}
	}
	
	public synchronized void añadirUsuairoLogado(Usuario u){
		System.out.println("se ha conectado: " + u.getNick());
		this.clientesLogados.add(u);
	}
	
	public synchronized void eliminarUsuarioLogado(Usuario u){
		this.clientesLogados.remove(u);
	}

	public synchronized boolean comprobarClienteLogado(String usuario){
		for (Usuario u : clientesLogados) {
			if(u.getUsuario().equals(usuario)){
				return true;
			}
		}
		return false;
	}
	
	public synchronized boolean comprobarNick(String nick){
		boolean valido = true;
		if(nick.contains(" ")){
			return false;
		}
		
		for(String s : this.PALABRAS_RESERVADAS){
			if(s.equals(nick)){
				return false;
			}
		}
		
		for(Usuario u : this.clientesLogados){
			if(u.getNick().equalsIgnoreCase(nick)){
				return false;
			}
		}
		return valido;
	}
	
	public void eliminarListaHilos(HiloCliente hc){
		this.listaClientes.remove(hc);
	}
}