
package ayuntamiento.viajes.exception;

/**
 * Controlador que crea una Excepcion para aquellas dentro del progama
 * que ocurren por la base de datos
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-06
 * @version 1.0
 */
public class APIException extends Exception {
    private final int statusCode;

    public APIException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

