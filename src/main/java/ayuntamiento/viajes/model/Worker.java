package ayuntamiento.viajes.model;

/**
 * Clase entidad que se encarga de los trabajadores (usuarios)
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class Worker {

    private long id;
    private String username;
    private String password;
    private long department;

    public Worker() {
    }

    public Worker(String username, String password, long department) {
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
    
    public long getDepartment(){
        return department;
    }
    public void setDepartment(long department){
        this.department = department;
    }

}
