package ayuntamiento.viajes.common;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase que se encarga de controlar los datos
 * definidos en el archivo de propiedades en la 
 * configuración del proyecto
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class PropertiesUtil {

    private static final Properties properties = new Properties();

    static {
        try  {
            InputStream input = 
                    PropertiesUtil.class.getClassLoader().getResourceAsStream("ayuntamiento/viajes/config/app.properties");
            if (input == null) {
                throw new IOException("Archivo de propiedades "
                        + "\"ayuntamiento/viajes/config/app.properties\" "
                        + "no encontrado");
            } else {
                properties.load(input);
            }
        } catch (IOException ioE) {
           log("Error al inciar las propiedades: \n"
                    + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }

    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
