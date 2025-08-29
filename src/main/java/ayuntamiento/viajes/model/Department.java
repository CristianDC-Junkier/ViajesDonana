package ayuntamiento.viajes.model;

import javafx.collections.ObservableList;

/**
 * Clase entidad que se encarga de los departamentos
 * 
 * @author Ramon Iglesias Granados
 * @since 2025-08-19
 * @version 1.0
 */
public class Department {

    private long id;
    private String name;

    private ObservableList<Long> travels;
    private ObservableList<Long> travellers;
    private long adminId;

    public Department() {
    }

    public Department(long id, String name, ObservableList<Long> travels, ObservableList<Long> travellers, long adminId) {
        this.id = id;
        this.name = name;
        this.travels = travels;
        this.travellers = travellers;
        this.adminId = adminId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableList<Long> getTravels() {
        return travels;
    }

    public void setTravels(ObservableList<Long> travels) {
        this.travels = travels;
    }

    public ObservableList<Long> getTravellers() {
        return travellers;
    }

    public void setTravellers(ObservableList<Long> travellers) {
        this.travellers = travellers;
    }

    public long getAdminId() {
        return adminId;
    }

    public void setAdminId(long adminId) {
        this.adminId = adminId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Department)) {
            return false;
        }
        Department that = (Department) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

}
