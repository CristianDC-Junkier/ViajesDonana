package ayuntamiento.viajes.model;

import ayuntamiento.viajes.common.Departments;
import javafx.collections.ObservableList;

/**
 *
 * @author Ramón Iglesias
 */
public class Department {

    private long id;
    private String name;

    private ObservableList<Long> travels;
    private ObservableList<Long> travellers;
    private long adminId;

    public Department() {
    }

    public Department(Departments department, ObservableList<Long> travels, ObservableList<Long> travellers, long adminId) {
        this.name = department.name();
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

    //@JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    public void setDepartament(Departments department) {
        this.setId(department.ordinal());
        this.setName(department.name());
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
