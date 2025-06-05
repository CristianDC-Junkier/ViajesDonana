package ayuntamiento.viajes.common;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase que Controla la transformación de las contraseñas en hash
 * y además la comprobaciones de strings
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.1
 */
public class SecurityUtil {

    /**
    * Cromprueba si una cadena es maliciosa o esta vacía
    * 
    * @param s cadena introducida
    * @return la verificación de que no es segura o si
    */
    public static boolean checkBadOrEmptyString(String s) {
        boolean check = false;
        if (checkBadString(s) || s.isBlank()) {
           check = true;
        }

        return check;
    }
    
    /**
    * Cromprueba si una contraseña es maliciosa
    * 
    * @param s cadena introducida
    * @return la verificación de que no es segura o si
    */
    public static boolean checkBadString(String s) {
        boolean check = false;
        if (s == null ) {
           check = true;
        }else{
             String lowered = s.toLowerCase();

            /* Lista de caracteres sospechosos */
            String[] dangerousPatterns = {
                "select ", "insert ", "update ", "delete ",
                "drop ", "alter ", "create ", "--", ";", "'",
                "\"", "/*", "*/", "xp_", "exec", "union "
            };

            for (String pattern : dangerousPatterns) {
                if (lowered.contains(pattern)) {
                    check = true;
                    break;
                }
            }
        }

        return check;
    }

}
