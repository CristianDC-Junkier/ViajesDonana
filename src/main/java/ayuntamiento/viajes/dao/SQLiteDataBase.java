package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.common.LoggerUtil;
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
 * Controlador que controla el acceso a la 
 * base de datos SQLite y su creación
 * 
 * @author Cristian
 * @since 2025-05-08
 * @version 1.2
 */
public class SQLiteDataBase {

    private static final String DB_PATH = System.getenv("APPDATA") + File.separator + PropertiesUtil.getProperty("DB_URL");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH + File.separator + "data.db";


    public static void create() {
        try {
            Files.createDirectories(Paths.get(DB_PATH));
        } catch (IOException e) {
            LoggerUtil.log("Error creando el directorio de la base de datos:");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Inicializa la Base de datos, creando las tablas si no existen
     * y añadiendo el usuario administador por defecto
     */
    public static void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS vehiculos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nameplate TEXT NOT NULL UNIQUE,
                    brand TEXT NOT NULL,
                    model TEXT NOT NULL,
                    type TEXT NOT NULL,
                    itv TEXT NOT NULL,
                    insurance TEXT NOT NULL
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

        } catch (SQLException e) {
            LoggerUtil.log("Error inicializando la base de datos, no se pudieron crear las tablas");
        }

        //Usuario Administrador
        
        String sql = "INSERT OR IGNORE INTO usuarios (tipo, usuario, contrasena) VALUES (?, ?, ?)";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, 1);
            pstmt.setString(2, "admin"); 
            pstmt.setString(3, SecurityUtil.hashPassword("admin"));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            LoggerUtil.log("Error iniciando la base de datos, no se pudo añadir el adiminstrador principal");
            e.printStackTrace();
        }

    }
}
