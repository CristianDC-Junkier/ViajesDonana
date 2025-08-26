package ayuntamiento.viajes.exception;
/**
 * Controlador que crea una Excepcion que no sale por pantalla al 
 * usuario
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-08-11
 * @version 1.0
 */
public class QuietException extends Exception{
    
     private final String class_metod;

    public QuietException(String message, String class_metod) {
        super(message);  
        this.class_metod = class_metod;
    }

    public String getWhere() {
        return class_metod;
    }
}
