
public class Usuario {
	
	private String usuario;
	private String nick;
	
	public Usuario(String user, String nick){
		this.usuario = user;
		this.nick = nick;
	}
	
	public String getNombreUsuario(){
		return this.usuario;
	}
	
	public String getNick(){
		return this.nick;
	}
	
	public void setNick(String nuevoNick){
		this.usuario = nuevoNick;
	}
}