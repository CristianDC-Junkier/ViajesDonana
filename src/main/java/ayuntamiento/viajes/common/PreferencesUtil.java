package ayuntamiento.viajes.common;

import ayuntamiento.viajes.model.Preferences;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Clase que se encarga de controlar las preferencias
 * de todos los datos sobre algunas
 * opciones elegida por el usuario
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-14
 * @version 1.0
 */
public class PreferencesUtil {

    private static final String PREF_PATH = System.getenv("APPDATA") + File.separator + PropertiesUtil.getProperty("PREF_PATH");
    private static final String PREFERENCES_FILE = "preferences.json";

    private static String FULLPATH;
    
    private static Preferences preferences;

    static {
        try {
            Files.createDirectories(Paths.get(PREF_PATH));
            FULLPATH = PREF_PATH + File.separator + PREFERENCES_FILE;
            
            loadPreferences();
        } catch (IOException e) {
            System.err.println("Error al inicializar las preferencias: " + e.getMessage());
        }
    }

    public static String getRemember() {
        return preferences.getRemember();
    }

    public static void setRemember(String user) {
        preferences.setRemember(user);
        savePreferences();
    }

    private static void loadPreferences() {

        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(FULLPATH);

            if (file.exists()) {
                preferences = mapper.readValue(file, Preferences.class);
            } else {
                // Ponemos todo en vacio por si no esta creado
                preferences = new Preferences();
                savePreferences();
            }
        } catch (IOException e) {
            System.err.println("No se pudo cargar las preferencias: " + e.getMessage());
        }
    }

    private static void savePreferences() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FULLPATH), preferences);
        } catch (IOException e) {
            System.err.println("No se pudo guardar las preferencias: " + e.getMessage());
        }
    }

}
