package ayuntamiento.viajes.common;

import ayuntamiento.viajes.app.App;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ramón Iglesias
 * @since 2025-05-21
 * @version 1.0
 */
public class ManagerUtil {

    private static String CURRENT;
    private static String PREVIOUS;

    private static final Map<String, String> TITLES = new HashMap<>() {{
        put("login", "Login");
        put("home", "Inicio");
        put("stadistics", "Estadísticas");
        put("vehicle", "Gestión de Vehículos");
        put("pdf", "Generador de PDFs");
        put("notification", "Gestión de Notificaciones");
        put("user", "Gestión de Usuarios");
        put("profile", "Mi Perfil");
        put("service_terms", "Terminos de Servicio");
    }};
    
    private static final Set<String> RESET_TO_HOME = Set.of("user", "profile", "service_terms");

    static {
        CURRENT = "login";
        PREVIOUS = "login";
    }

    
    
    public static void moveTo(String fxml) throws IOException {
        PREVIOUS = CURRENT;
        CURRENT = fxml;

        String title = TITLES.getOrDefault(fxml, capitalize(fxml));
        App.setRoot(fxml, title);
    }

    public static void goBack() throws IOException {
        String temp = CURRENT;
        CURRENT = PREVIOUS;
        
        App.setRoot(PREVIOUS, TITLES.getOrDefault(PREVIOUS, capitalize(PREVIOUS)));

        if (RESET_TO_HOME.contains(temp)) {
            PREVIOUS = "home";
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

