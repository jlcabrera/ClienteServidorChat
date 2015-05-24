
public enum CodigosError {
	CONEXION_REALIZADA(200, "Conexion realizada con exito"),
	USUARIO_CORRECTO(202, "El usuario se ha autenticado con exito"),
	USUARIO_ERRONEO(503, "Fallo en la autenticacion"),
	TIME_OUT(504, "Tiempo de espera agotado");
	
	
	private final int cod;
	private final String mensaje;
	
	CodigosError(int codigo, String mensaje){
		this.cod = codigo;
		this.mensaje = mensaje;
	}
	
	public int getCodigo(){
		return this.cod;
	}
	
	public String getMensaje(){
		return this.mensaje;
	}
}
