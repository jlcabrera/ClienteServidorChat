import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Servidor {

	private List<HiloCliente> listaClientes = new ArrayList<HiloCliente>();
	private GestorUsuarios gu = new GestorUsuarios();
	private List<String> nicksRegistrados = new ArrayList<String>();
	private final String [] PALABRAS_RESERVADAS = { "nick", "msg", "login", "quit", "userlist", "ping"};
	public static void main(String[] args) {
		new Servidor();
	}
	
	public Servidor(){
		ServerSocket serverSocket = null;
		try {
			
			serverSocket = new ServerSocket(6667);
			while(true){
				Socket socket = serverSocket.accept();
				this.listaClientes.add(new HiloCliente(socket, this));
			}
		 }catch (IOException e) {
			e.printStackTrace();
		 }finally{
			 if(serverSocket != null){
				 try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		 }
	}
	
	public synchronized void enviarATodos(HiloCliente hiloCliente, String mensaje){
		for(HiloCliente hc : this.listaClientes ){
			if (hc != hiloCliente){
				hc.escribir(mensaje);
			}
		}
	}
	
	public synchronized Usuario addUsuairoLogado(String usuario, String nick){
		Usuario user = this.gu.addUsuario(usuario, nick);
		System.out.println("se ha conectado: " + nick);
		return user;
	}
	
	public synchronized GestorUsuarios getGestorUsuario(){
		return this.gu;
	}
	public synchronized void eliminarUsuarioLogado(Usuario u){
		this.gu.eliminarUsuario(u);
	}

	public synchronized boolean comprobarClienteLogado(String usuario){
		for (Usuario u : this.gu.getListaUsuarios()) {
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
		
		for(Usuario u : this.gu.getListaUsuarios()){
			if(u.getNick().equalsIgnoreCase(nick)){
				return false;
			}
		}
		
		return valido;
	}
	
	//Metodos para la administracion de nicks
	public void addNick(String nick){
		this.nicksRegistrados.add(nick);
	}
	
	public void eliminarNick(String nick){
		this.nicksRegistrados.remove(nick);
	}
	
	public void eliminarListaHilos(HiloCliente hc, String nick){
		enviarATodos(hc, "Se ha desconectado el usuario: " + nick);
		this.listaClientes.remove(hc);
	}
	
	//Metodo para enviar a un usuario los mensajes privadamente
	public void enviarAUsuario(String nick, String mensaje){
		for(HiloCliente hc : this.listaClientes ){
			if(hc.getUsuario().getNick().equalsIgnoreCase(nick)){
				hc.escribir(mensaje);
			}
		}
	}
}