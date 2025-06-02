package ayuntamiento.viajes.dao;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.common.SecurityUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Controlador que controla el acceso a la base de datos SQLite y su creación
 *
 * @author Cristian
 * @since 2025-05-08
 * @version 1.2
 */
public class SQLiteDataBase {

    private static final String DB_PATH = System.getenv("APPDATA") 
            + File.separator + PropertiesUtil.getProperty("DB_URL");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH 
            + File.separator + "data.db";

    public static void create() {
        try {
            Files.createDirectories(Paths.get(DB_PATH));
        } catch (IOException ioE) {
            log("Error al crear la Base de datos: \n" + ioE.getMessage() + "\n"
                    + ioE.getCause());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Inicializa la Base de datos, creando las tablas si no existen y añadiendo
     * el usuario administador por defecto
     */
    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS vehiculos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numplate TEXT NOT NULL UNIQUE,
                    vehicle TEXT NOT NULL,
                    destination TEXT,
                    type TEXT NOT NULL,
                    status TEXT NOT NULL,
                    allocation TEXT,
                    kms_last_check INTEGER,
                    last_check TEXT,
                    itv_rent TEXT,
                    insurance TEXT
                );
            """;
            stmt.execute(sql);

            sql = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo INTEGER NOT NULL,
                    usuario TEXT NOT NULL UNIQUE,
                    contrasena TEXT NOT NULL
                );
            """;
            stmt.execute(sql);

        } catch (SQLException sqlE) {
            log("Error inicializando la base de datos, no se pudieron crear las tablas: \n" + sqlE.getMessage() + "\n"
                    + sqlE.getCause());
        }

        /* Usuario Administrador */
        String sql = "INSERT OR IGNORE INTO usuarios (tipo, usuario, contrasena) VALUES (?, ?, ?)";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, 1);
            pstmt.setString(2, "admin");
            pstmt.setString(3, SecurityUtil.hashPassword("Almonte@admin"));

            pstmt.executeUpdate();

        } catch (SQLException sqlE) {
            log("Error iniciando la base de datos, no se pudo añadir el administrador principal: \n" + sqlE.getMessage() + "\n"
                    + sqlE.getCause());

        }

    }
}
