
package ayuntamiento.viajes.exception;

/**
 * Controlador que crea una Excepcion para el login
 * siendo 1000 fallo de usuario y 2000 fallo de contraseña
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-05-12
 * @version 1.0
 */
public class LoginException extends Exception {
     
    private final int errorCode;

    public LoginException(String message, int errorCode) {
        super(message);  
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
