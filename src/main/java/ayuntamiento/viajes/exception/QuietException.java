package ayuntamiento.viajes.exception;
/**
 *
 * @author USUARIO
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
