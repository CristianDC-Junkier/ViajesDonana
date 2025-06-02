package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Traveller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los vehiculos
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class TravellerDAO implements IDao<Traveller> {

    @Override
    public Traveller save(Traveller traveller) throws SQLException {
        String sql = "INSERT INTO travellers (dni, name, singup, office, trip)"
                + "VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, traveller.getDni());
            stmt.setString(2, traveller.getName());
            /*Object porque si no, no se come el nulo*/
            stmt.setObject(3, traveller.getSignUp());
            stmt.setInt(4, traveller.getOffice().ordinal());
            stmt.setInt(5, traveller.getTrip().ordinal());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el viajero, ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    traveller.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del viajero insertado.");
                }
            }
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }
        return traveller;
    }

    @Override
    public Traveller modify(Traveller traveller) throws SQLException {
        String sql = "UPDATE travellers SET dni = ?, name = ?, singup = ?, "
                + "office = ?, trip = ? WHERE id = ?";
        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, traveller.getDni());
            stmt.setString(2, traveller.getName());
            stmt.setString(3, traveller.getSignUp());
            stmt.setInt(4, traveller.getOffice().ordinal());
            stmt.setInt(5, traveller.getTrip().ordinal());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el viajero,"
                        + " ninguna fila afectada.");
            }
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }
        return traveller;
    }

    @Override
    public boolean delete(Traveller entity) throws SQLException {
        String sql = "DELETE FROM travellers WHERE id = ?";
        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, entity.getId());
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (Exception ex) {
            throw new SQLException("No se pudo eliminar el viajero " + entity.getDni());
        }
    }

    @Override
    public List<Traveller> findAll() throws SQLException {
        List<Traveller> listV = new ArrayList<>();
        String sql = "SELECT * FROM travellers";

        try (Connection conn = SQLiteDataBase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Traveller v = new Traveller();
                v.setId(rs.getInt("id"));
                v.setDni(rs.getString("dni"));
                v.setName(rs.getString("name"));
                v.setSignUp(rs.getString("singup"));
                v.setOffice(rs.getInt("office"));
                v.setTrip(rs.getInt("trip"));
                listV.add(v);
            }
        } catch (Exception ex) {
            throw new SQLException("No se pudo recoger la lista de viajeros.");
        }

        return listV;
    }

    @Override
    public Traveller findById(int id) throws SQLException {
        Traveller t = null;
        String sql = "SELECT * FROM travellers WHERE id = ?";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    t = new Traveller();
                    t.setId(rs.getInt("id"));
                    t.setDni(rs.getString("dni"));
                    t.setName(rs.getString("name"));
                    t.setSignUp(rs.getString("singup"));
                    t.setOffice(rs.getInt("office"));
                    t.setTrip(rs.getInt("trip"));
                }
            }
        } catch (Exception ex) {
            throw new SQLException("No se pudo encontrar el viajero con ID: " + id);
        }

        return t;
    }

}
