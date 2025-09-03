package ayuntamiento.viajes.common;

import ayuntamiento.viajes.model.Worker;
import ayuntamiento.viajes.service.LoginService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Clase que se se encarga de escribir los diferentes logs de la aplicación para
 * registrar los movimientos de cada usuario y sus fallos.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class LoggerUtil {

    private static final String LOG_PATH = System.getenv("APPDATA") 
            + File.separator + PropertiesUtil.getProperty("LOG_PATH");
    private static final String DATE_FORMAT = 
            PropertiesUtil.getProperty("DATE_FORMAT");
    private static final String TIME_FORMAT = 
            PropertiesUtil.getProperty("TIME_FORMAT");

    private static final int MAX_LOGS = 
            Integer.parseInt(PropertiesUtil.getProperty("MAX_LOGS"));
    private static String FULLPATH;
    private static int CONNECTIONCOUNT = 1;

    /**
     * Metodo estático que se encarga
     * de crear el directorio de los logs, 
     * asignar a la dirección/nombre del log
     * la fecha de hoy, y eliminar los logs antiguos
     *
     */
    static {
        try {
            Files.createDirectories(Paths.get(LOG_PATH));

            String fechaHoy = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            FULLPATH = LOG_PATH + File.separator + "log_" + fechaHoy + ".txt";
            clearOldLogs();

        } catch (IOException ioE) {
            System.err.println("Error al recoger el directorio del log: " + ioE.getMessage());
        }
    }

    /**
     * Inicializa o carga el log de hoy, contando las conexiones previas
     *
     */
    public static void iniLog() {
        try {
            File logFile = new File(FULLPATH);
            String fechaActual = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String horaActual = new SimpleDateFormat(TIME_FORMAT).format(new Date());

            if (!logFile.exists()) {
                /* Si el log no existe, se crea y se le 
                coloca el nombre de "Conexión 1" */
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {

                    writer.write("Log (" + CONNECTIONCOUNT + ") - Fecha: " + fechaActual + " Hora: " + horaActual + "\n");
                    writer.write("--------------------------\n");
                }
            } else {
                /* Si el log ya existe, contar conexiones previas */
                List<String> lineas = Files.readAllLines(logFile.toPath());
                for (String linea : lineas) {
                    if (linea.startsWith("Log (" + CONNECTIONCOUNT + ")")) {
                        CONNECTIONCOUNT++;
                    }
                }
                /* Agregar nueva conexión */
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                    writer.write("\nLog (" + CONNECTIONCOUNT + ") - Fecha: " + fechaActual + " Hora: " + horaActual + "\n");
                    writer.write("--------------------------\n");
                }
            }
        } catch (IOException ioE) {
            System.err.println("Error al inicializar el log: " + ioE.getMessage());
        }
    }

    /**
     * Elimina los logs más antiguos si exceden el número máximo permitido
     */
    private static void clearOldLogs() {
        File logDir = new File(LOG_PATH);
        File[] archivos = logDir.listFiles((dir, name) -> 
                name.startsWith("log_") && name.endsWith(".txt"));
        if (archivos != null && archivos.length > MAX_LOGS) {
            /*Ordenar archivos por fecha
            (más antiguos primero) para eliminarlos*/
            Arrays.sort(archivos, Comparator.comparingLong(File::lastModified));
            archivos[0].delete();
        }
    }

    /**
     * Escribe un mensaje en el archivo de log 
     *
     * @param mensaje mensaje que se escribe en el log
     */
    public static void log(String mensaje) {
        /*Comprobar si el usuario ya inició sesión*/
        Worker user = LoginService.getAccountLog();
        if (LoginService.getAccountLog() == null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FULLPATH, true))) {
                String timestamp = new SimpleDateFormat(TIME_FORMAT).format(new Date());
                writer.write("Not Login - " + timestamp + " - " + mensaje + "\n");
            } catch (IOException ioE) {
                System.err.println("Error al escribir en el "
                        + "log sin usuario: " + ioE.getMessage());
            }
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FULLPATH, true))) {
                String timestamp = new SimpleDateFormat(TIME_FORMAT).format(new Date());
                writer.write("User: " + user.getUsername() + " - " + timestamp 
                        + " - " + mensaje + "\n");
            } catch (IOException ioE) {
                System.err.println("Error al escribir en el "
                        + "log con usuario, " + user.getUsername() 
                        + " en: " + ioE.getMessage());
            }
        }
    }
}
