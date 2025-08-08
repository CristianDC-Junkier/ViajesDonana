package ayuntamiento.viajes.model;

/**
 * Clase entidad que se encarga de los administradores (usuarios)
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class Admin {

    private long id;
    private String username;
    private String password;
    private Department department;

    public Admin() {
    }

    public Admin(String username, String password, Department department) {
        this.username = username;
        this.password = password;
        this.department = department;
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
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Department getDepartment(){
        return department;
    }
    public void setDepartment(Department department){
        this.department = department;
    }

}
