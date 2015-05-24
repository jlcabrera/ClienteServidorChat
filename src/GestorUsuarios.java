import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;


public class GestorUsuarios {
	
	private static GestorUsuarios instance = new GestorUsuarios();
	private TreeMap<String, Usuario> clientesLogados =  new TreeMap<String, Usuario>();
	
	public GestorUsuarios(){
	}
	
	public static GestorUsuarios getInstance(){
		return instance;
	}
	
	public Usuario addUsuario(String usuario, String nick){
		Usuario user = new Usuario(usuario,nick);
		this.clientesLogados.put(nick, user);
		return user;
	}
	
	public void eliminarUsuario(String nick){
		this.clientesLogados.remove(nick);
	}
	
	public Set<String> getListaUsuarios(){
		Set<String> ListaUsuarios = this.clientesLogados.keySet();
		return ListaUsuarios;
	}
	
	public void setNick(String nickAntiguo, String nickNuevo){
		Usuario user = this.clientesLogados.get(nickAntiguo);
		this.clientesLogados.remove(nickAntiguo);
		this.clientesLogados.put(nickNuevo, user);
	}
	
	public Collection<Usuario> getUsuarios(){
		return this.clientesLogados.values();
	}
	
	
	
}
