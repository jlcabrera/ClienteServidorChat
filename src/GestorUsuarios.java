import java.util.ArrayList;
import java.util.List;


public class GestorUsuarios {
	private List<Usuario> clientesLogados;
	
	
	public GestorUsuarios(){
		this.clientesLogados = new ArrayList<Usuario>();
	}
	
	public Usuario addUsuario(String usuario, String nick){
		Usuario user = new Usuario(usuario,nick);
		this.clientesLogados.add(user);
		return user;
	}
	
	public void eliminarUsuario(Usuario user){
		this.clientesLogados.remove(user);
	}
	
	public List<Usuario> getListaUsuarios(){
		return this.clientesLogados;
	}
	
	public void setNick(String nickAntiguo, String nickNuevo){
		for(Usuario s : this.clientesLogados){
			if(s.getNick().equalsIgnoreCase(nickAntiguo)){
				s.setNick(nickNuevo);
			}
		}
	}
	
}
