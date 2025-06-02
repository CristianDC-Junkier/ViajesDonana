package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Admin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los usarios
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class AdminDAO implements IDao<Admin> {

    @Override
    public Admin save(Admin entity) throws SQLException {
        String sql = "INSERT INTO admins (nickname, password) VALUES (?, ?)";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, entity.getUsername());
            pstmt.setString(2, entity.getPassword());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el administrador, ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del administrador insertado.");
                }
            }

        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }

        return entity;
    }

    @Override
    public Admin modify(Admin entity) throws SQLException {
        String sql = "UPDATE admins SET nickname = ?, password = ? WHERE id = ?";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entity.getUsername());
            pstmt.setString(2, entity.getPassword());
            pstmt.setLong(3, entity.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el administrador, ninguna fila afectada.");
            }

        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }

        return entity;
    }

    @Override
    public boolean delete(Admin entity) throws SQLException {
        String sql = "DELETE FROM admins WHERE id = ?";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, entity.getId());
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException sqlE) {
            throw new SQLException("No se pudo eliminar el administrador " + entity.getUsername());
        }
    }

    @Override
    public List<Admin> findAll() throws SQLException {
        List<Admin> listA = new ArrayList<>();
        String sql = "SELECT * FROM admins";

        try (Connection conn = SQLiteDataBase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Admin u = new Admin();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("nickname"));
                u.setContraseña(rs.getString("password"));
                listA.add(u);
            }
        } catch (Exception ex) {
            throw new SQLException("No se pudo recoger la lista de administradores.");
        }

        return listA;
    }

    @Override
    public Admin findById(int id) throws SQLException {
        Admin a = null;
        String sql = "SELECT * FROM admins WHERE id = ?";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    a = new Admin();
                    a.setId(rs.getInt("id"));
                    a.setUsername(rs.getString("nickname"));
                    a.setContraseña(rs.getString("password"));
                }
            }
        } catch (Exception ex) {
            throw new SQLException("No se pudo encontrar el viajero con ID: " + id);
        }

        return a;
    }

}
