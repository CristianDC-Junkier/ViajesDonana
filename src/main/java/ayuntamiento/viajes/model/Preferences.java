package ayuntamiento.viajes.model;

/**
 * Clase entidad que se utiliza para guardar 
 * las preferencias en un JSON
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-05-14
 * @version 1.0
 */
public class Preferences {

    /**
    * Usuario recordado por el programa
    */
    private String remember;

    public Preferences(){
        this.remember = "";
    }
    
    public Preferences(String remember) { this.remember = remember; }
    
    public String getRemember() { return remember; }

    public void setRemember(String remember) { this.remember = remember; }
}
