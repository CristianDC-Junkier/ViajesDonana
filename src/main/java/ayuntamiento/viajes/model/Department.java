package ayuntamiento.viajes.model;

import ayuntamiento.viajes.common.Departments;
import javafx.collections.ObservableList;

/**
 *
 * @author Ramón Iglesias
 */
public class Department {

    private long id;
    private Departments name;
    
    private ObservableList<Long> travels;
    private ObservableList<Long> travellers;
    private long adminId;
    
    

    public Department() {

    }
    
    public Department(Integer name, ObservableList<Long> travels, ObservableList<Long> travellers, long adminId){
        this.name = Departments.values()[name];
        this.travels = travels;
        this.travellers = travellers;
        this.adminId = adminId;
    }
    
    public long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
    
    //@JsonGetter("name")
    public int getNameOrdinal() {
        return name.ordinal();
    }
    public Departments getName() {
        return name;
    }
    //@JsonSetter("name")
    public void setOffice(Integer name) {
        this.name = Departments.values()[name];
    }
    
    public ObservableList<Long> getTravels(){
        return travels;
    }
    public void setTravels(ObservableList<Long> travels){
        this.travels = travels;
    }
    
    public ObservableList<Long> getTravellers(){
        return travellers;
    }
    public void setTravellers(ObservableList<Long> travellers){
        this.travellers = travellers;
    }
    
    public long getAdminId(){
        return adminId;
    }
    public void setAdminId(long adminId){
        this.adminId = adminId;
    }
}
