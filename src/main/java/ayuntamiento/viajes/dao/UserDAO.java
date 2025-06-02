package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.User;
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
 * @author Cristian
 * @since 2025-05-09
 * @version 1.2
 */
public class UserDAO implements IDao<User> {

    @Override
    public User save(User entity) throws SQLException{
        String sql = "INSERT INTO usuarios (tipo, usuario, contrasena) VALUES (?, ?, ?)";

        try (Connection conn = SQLiteDataBase.getConnection(); 
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, entity.getType().ordinal());
            pstmt.setString(2, entity.getUsername());
            pstmt.setString(3, entity.getPassword());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el usuario, ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del usuario insertado.");
                }
            }

        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }

        return entity;
    }

    @Override
    public User modify(User entity) throws SQLException{
        String sql = "UPDATE usuarios SET tipo = ?, usuario = ?, contrasena = ? WHERE id = ?";

        try (Connection conn = SQLiteDataBase.getConnection(); 
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, entity.getType().ordinal());
            pstmt.setString(2, entity.getUsername());
            pstmt.setString(3, entity.getPassword());
            pstmt.setLong(4, entity.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el usuario, ninguna fila afectada.");
            }

        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }

        return entity;
    }

    @Override
    public boolean delete(User entity) throws SQLException{
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conn = SQLiteDataBase.getConnection(); 
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, entity.getId());
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException sqlE) {
            throw new SQLException("No se pudo eliminar el usuario " + entity.getUsername());
        }
    }

    @Override
    public List<User> findAll() throws SQLException{
        List<User> listU = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";

        try (Connection conn = SQLiteDataBase.getConnection(); 
                Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User u = new User(rs.getInt("tipo"),
                        rs.getString("usuario"),
                        rs.getString("contrasena"));
                u.setId(rs.getInt("id"));
                listU.add(u);
            }
        } catch (Exception ex) {
            throw new SQLException("No se pudo recoger la lista de usuarios.");
        }

        return listU;
    }

}
