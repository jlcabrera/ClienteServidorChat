import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class RegistroUsuarios {
	
	private BufferedReader entrada;

	public RegistroUsuarios(){
		try {
			this.entrada = new BufferedReader(new FileReader("Usuarios.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean verificarUsuario(String usuario, String pass){
		
		try {
			String linea = "";
			while((linea = this.entrada.readLine()) != null){
				String [] line = linea.split("|");
				if(usuario.equalsIgnoreCase(line[0]) && pass.equalsIgnoreCase(line[1])){
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				this.entrada.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
