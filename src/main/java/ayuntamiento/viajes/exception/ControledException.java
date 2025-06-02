
package ayuntamiento.viajes.exception;

/**
 * Controlador que crea una Excepcion para aquellas dentro del progama
 * que controlemos nosotros mismos
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-05-28
 * @version 1.0
 */
public class ControledException extends Exception{
    
    private final String class_metod;

    public ControledException(String message, String class_metod) {
        super(message);  
        this.class_metod = class_metod;
    }

    public String getWhere() {
        return class_metod;
    }
}
