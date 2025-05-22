package ayuntamiento.viajes.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase que se encarga de controlar los datos
 * definidos en el archivo de propiedades en la 
 * configuración del proyecto
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-14
 * @version 1.0
 */
public class PropertiesUtil {

    private static final Properties properties = new Properties();

    static {
        try  {
            InputStream input = 
                    PropertiesUtil.class.getClassLoader().getResourceAsStream("ayuntamiento/viajes/config/app.properties");
            if (input == null) {
                System.err.println("Archivo de propiedades no encontrado.");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
