
package ayuntamiento.viajes.exception;

/**
 * Controlador que crea una Excepcion para el login
 * siendo 1000 fallo de usuario y 2000 fallo de contraseña
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-05
 * @version 1.0
 */
public class ReloadException extends Exception {
     
    private final boolean recover;

    public ReloadException(String message, boolean recover) {
        super(message);  
        this.recover = recover;
    }

    public boolean wasRecovered() {
        return recover;
    }
}
