package ayuntamiento.viajes.common;

import ayuntamiento.viajes.app.App;
import static ayuntamiento.viajes.common.LoggerUtil.log;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Clase que se se encarga del manejo de los nombres de los fxml, sus titulos y
 * su tamaño de ventana
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-02
 * @version 1.0
 */
public class ManagerUtil {

    private static String CURRENT;
    private static String PREVIOUS;

    /*Lista de las diferentes vistas*/
    private static final Map<String, Map<String, Object>> PAGE_INFO = new HashMap<>() {
        {
            put("login", Map.of("title", "Login", "width", 1000, "height", 800));
            put("home", Map.of("title", "Inicio", "width", 1000, "height", 800));
            put("stadistics", Map.of("title", "Estadísticas", "width", 1000, "height", 800));
            put("traveller", Map.of("title", "Gestión de Inscripciones", "width", 1000, "height", 800));
            put("travel", Map.of("title", "Gestión de Viajes", "width", 1000, "height", 800));
            put("pdf", Map.of("title", "Generador de PDFs", "width", 1000, "height", 800));
            put("admin", Map.of("title", "Gestión de Usuarios", "width", 1000, "height", 800));
            put("profile", Map.of("title", "Mi Perfil", "width", 1000, "height", 800));
            put("service_terms", Map.of("title", "Terminos de Servicio", "width", 1000, "height", 800));
        }
    };

    /*Lista de las páginas que siempre llevan al home, aunque sean invocadas
    desde otra parte*/
    private static final Set<String> RESET_TO_HOME
            = Set.of("admin", "profile", "service_terms");

    /*Valor inicial de las páginas actual y previa*/
    static {
        CURRENT = "login";
        PREVIOUS = "login";
    }

    public static String getPage() {
        return CURRENT;
    }

    /**
     * Funcion para moverse entre páginas
     *
     * @param fxml vista a la que nos movemos
     */
    public static void moveTo(String fxml) {
        try {

            Map<String, Object> page = PAGE_INFO.get(fxml);
            App.setRoot(fxml, (String) page.get("title"));
            App.setDim((int) page.get("width"), (int) page.get("height"));

            PREVIOUS = CURRENT;
            CURRENT = fxml;
        } catch (IOException ioE) {
            log("Error al moverse entre páginas - Desde " + PREVIOUS + " a "
                    + CURRENT
                    + ": \n" + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }
    }

    /**
     * Funcion para volver atras
     */
    public static void goBack() {
        try {

            Map<String, Object> page = PAGE_INFO.get(PREVIOUS);
            App.setRoot(PREVIOUS, (String) page.get("title"));
            App.setDim((int) page.get("width"), (int) page.get("height"));

            String temp = CURRENT;
            CURRENT = PREVIOUS;

            if (RESET_TO_HOME.contains(temp)) {
                PREVIOUS = "home";
            } else {
                PREVIOUS = temp;
            }
        } catch (IOException ioE) {
            log("Error al volver a atras - Desde " + CURRENT + " a "
                    + PREVIOUS
                    + ": \n" + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }
    }

    /**
     * Funcion para volver al principio
     */
    public static void reload() {
        try {

            CURRENT = "login";
            PREVIOUS = "login";

            Map<String, Object> page = PAGE_INFO.get(CURRENT);
            App.setRoot(PREVIOUS, (String) page.get("title"));
            App.setDim((int) page.get("width"), (int) page.get("height"));

        } catch (IOException ioE) {
            log("Error al resetear: \n" 
                    + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }
    }

}
