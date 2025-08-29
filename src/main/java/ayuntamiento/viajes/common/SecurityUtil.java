package ayuntamiento.viajes.common;

/**
 * Clase que comprueba las Strings
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.2
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
     * Cromprueba si una cadena es maliciosa
     *
     * @param s cadena introducida
     * @return la verificación de que no es segura o si
     */
    public static boolean checkBadString(String s) {
        boolean check = false;
        if (s == null) {
            check = true;
        } else {
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

    /**
     * Cromprueba si un dni/nie existe
     *
     * @param s dni/nie introducida
     * @return la verificación de que no es segura o si
     */
    public static boolean checkDNI_NIE(String s) {
        String value = s.trim().toUpperCase();

        // NIE comienza con X, Y o Z
        if (value.matches("[XYZ]\\d{7}[A-Z]")) {
            char firstChar = value.charAt(0);
            String number = switch (firstChar) {
                case 'X' ->
                    "0" + value.substring(1, 8);
                case 'Y' ->
                    "1" + value.substring(1, 8);
                case 'Z' ->
                    "2" + value.substring(1, 8);
                default ->
                    value;
            };
            return checkLetter(number, value.charAt(8));
        } 
        else if (value.matches("\\d{8}[A-Z]")) {
            return checkLetter(value.substring(0, 8), value.charAt(8));
        }

        return false;
    }

    private static boolean checkLetter(String numberStr, char realLetter) {
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        try {
            return realLetter == letras.charAt(Integer.parseInt(numberStr) % 23);
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
