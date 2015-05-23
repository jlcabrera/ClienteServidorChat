import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Servidor {

	private List<HiloCliente> listaClientes = new ArrayList<HiloCliente>();
	private GestorUsuarios gestor = GestorUsuarios.getInstance();
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
	
	public synchronized Usuario addUsuarioLogado(String usuario, String nick){
		System.out.println("se ha conectado: " + nick);
		return this.gestor.addUsuario(usuario, nick);
	}
	
	public synchronized GestorUsuarios getGestorUsuario(){
		return this.gestor;
	}
	public synchronized void eliminarUsuarioLogado(String nick){
		this.gestor.eliminarUsuario(nick);
	}
	
	public ArrayList<String> listarUsuarios(){
		return new ArrayList<String>(this.gestor.getListaUsuarios());
	}

	public synchronized boolean comprobarClienteLogado(String u){
		ArrayList<Usuario> usuarios = new ArrayList<Usuario>(this.gestor.getUsuarios());
		for (Usuario user : usuarios) {
			if(u.equals(user.getNombreUsuario())){
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
		
		for(String nicksUsados : this.gestor.getListaUsuarios()){
			if(nicksUsados.equalsIgnoreCase(nick)){
				return false;
			}
		}
		return valido;
	}
	
	public void eliminarListaHilos(HiloCliente hc, String nick){
		enviarATodos(hc, "Se ha desconectado el usuario: " + nick);
		this.listaClientes.remove(hc);
	}
	
	//Metodo para enviar a un usuario los mensajes privadamente
	public boolean enviarAUsuario(String nick, String mensaje){
		for(HiloCliente hc : this.listaClientes){
			if(hc.getUsuario().getNick().equalsIgnoreCase(nick)){
				hc.escribir(mensaje);
				return true;
			}
		}
		return false;
	}

	public synchronized void enviarATodos(HiloCliente hiloCliente, String mensaje){
		for(HiloCliente hc : this.listaClientes ){
			if (hc != hiloCliente){
				hc.escribir(hiloCliente.getUsuario().getNick() + " : " +mensaje);
			}
		}
	}
	
	
}