package ayuntamiento.viajes.model;

/**
 * Clase entidad que se encarga de los usuarios
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class Admin {

    private long id;
    private String username;
    private String password;

    public Admin() {
    }

    public Admin(String usuario, String contrasena) {
        this.username = usuario;
        this.password = contrasena;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
