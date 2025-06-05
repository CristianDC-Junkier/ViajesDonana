package ayuntamiento.viajes.common;

import static ayuntamiento.viajes.common.LoggerUtil.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Clase estatica que controla la 
 * creación de los BackUps de SQLite
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class BackupUtil {

    private static final String DB_URL = System.getenv("APPDATA") 
            + File.separator + PropertiesUtil.getProperty("DB_URL") 
            + File.separator + "data.db";
    private static final String DATE_FORMAT = 
            PropertiesUtil.getProperty("DATE_FORMAT");
    private static final String BUP_PATH = System.getenv("APPDATA") 
            + File.separator + PropertiesUtil.getProperty("BUP_PATH");

    private static final int MAX_BUP = 
            Integer.parseInt(PropertiesUtil.getProperty("MAX_BUP"));
    private static final String FULLPATH;

     /**
     * Metodo estático que se encarga
     * de crear el directorio de los archivos, 
     * asignar a la dirección/nombre del archivo
     * la fecha de hoy, y eliminar los archivos antiguos
     *
     */
    static {
        createBackupDir();
        String fechaHoy = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        FULLPATH = BUP_PATH + File.separator + "backup_" + fechaHoy + ".db";
        clearOldBackUps();
    }
    
     /**
     * Crear el backup actual
     *
     */
    public static void createBackup() {
        Path source = Paths.get(DB_URL);
        Path target = Paths.get(FULLPATH);

        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioE) {
            log("Error al copiar la Base de datos: \n" + ioE.getMessage() + "\n" 
                    + ioE.getCause());
        }
    }
    
    /**
     * Crear el Backup dir
     *
     */
    private static void createBackupDir() {
        Path backupDir = Paths.get(BUP_PATH);
        try {
            Files.createDirectories(backupDir);
        } catch (IOException ioE) {
            log("Error al crear el directorio de Backups: \n" + ioE.getMessage() + "\n" 
                    + ioE.getCause());
        }
    }

    /**
     * Elimina los Backups más antiguos si exceden el número máximo permitido
     * 
     */
    private static void clearOldBackUps() {
        File logDir = new File(BUP_PATH);
        File[] archivos = logDir.listFiles((dir, name) -> 
                name.startsWith("backup_") && name.endsWith(".db"));
        if (archivos != null && archivos.length > MAX_BUP) {
            Arrays.sort(archivos, Comparator.comparingLong(File::lastModified));
            archivos[0].delete();
        }
    }
}
