package ayuntamiento.viajes.common;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import ayuntamiento.viajes.model.Preferences;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Clase que se encarga de controlar las preferencias de todos los datos sobre
 * algunas opciones elegida por el usuario
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class PreferencesUtil {

    private static final String PREF_PATH = System.getenv("APPDATA")
            + File.separator + PropertiesUtil.getProperty("PREF_PATH");
    private static final String PREFERENCES_FILE = "preferences.json";

    private static String FULLPATH;

    private static Preferences preferences;

    static {
        try {
            Files.createDirectories(Paths.get(PREF_PATH));
            FULLPATH = PREF_PATH + File.separator + PREFERENCES_FILE;

            loadPreferences();
        } catch (IOException ioE) {
            log("Error al inciar las preferencias: \n"
                    + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }
    }

    /**
     * Metodo que devuelve las preferencias
     *
     * @return las preferencias
     */
    public static Preferences getPreferences() {
        return preferences;
    }

    /**
     * Metodo que guarda las preferencias
     *
     * @param pref las preferencias
     */
    public static void setPreferences(Preferences pref) {
        preferences = pref;
        savePreferences();
    }

    private static void loadPreferences() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        File file = new File(FULLPATH);

        if (file.exists()) {
            preferences = mapper.readValue(file, Preferences.class);
        } else {
            preferences = new Preferences();
            preferences.setRemember(null);
            //savePreferences();
        }
    }

    private static void savePreferences() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FULLPATH), preferences);
        } catch (IOException ioE) {
            log("Error al guardar las preferencias: \n" 
                    + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }
    }

}
