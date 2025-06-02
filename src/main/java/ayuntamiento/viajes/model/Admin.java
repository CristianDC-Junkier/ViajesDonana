package ayuntamiento.viajes.model;

/**
 * Clase entidad que se encarga de los usuarios
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-05-08
 * @version 1.0
 */
public class Admin {

    private long id;
    private UserType type;
    private String username;
    private String password;

    public enum UserType {
        TRABAJADOR,
        ADMINISTRADOR
    }

    public Admin() {
    }

    public Admin(int tipo, String usuario, String contrasena) {
        this.type = UserType.values()[tipo];
        this.username = usuario;
        this.password = contrasena;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserType getType() {
        return type;
    }

    public void setTipo(int tipo) {
        this.type = UserType.values()[tipo];
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setContraseña(String contrasena) {
        this.password = contrasena;
    }

}
