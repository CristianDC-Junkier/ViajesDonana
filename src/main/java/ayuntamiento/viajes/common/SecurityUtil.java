package ayuntamiento.viajes.common;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase que Controla la transformación de las contraseñas en hash
 * y además la comprobaciones de strings
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-05-13
 * @version 1.1
 */
public class SecurityUtil {

    public static String hashPassword(String passwordFlat) {
        return BCrypt.hashpw(passwordFlat, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String passwordFlat, String hash) {
        return BCrypt.checkpw(passwordFlat, hash);
    }

    public static boolean checkBadString(String string) {
        boolean check = false;
        if (string == null || string.isBlank()) {
           check = true;
        }else{
             String lowered = string.toLowerCase();

            // Lista de caracteres sospechosos
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
