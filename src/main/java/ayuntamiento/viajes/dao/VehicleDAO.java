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
 * @author Ramón Iglesias Granados
 * @since 2025-05-09
 * @version 1.2
 */
public class VehicleDAO implements IDao<Traveller> {

    @Override
    public Traveller save(Traveller vehiculo) throws SQLException {
        String sql = "INSERT INTO vehiculos (numplate, vehicle, destination, type, "
                + "status, allocation, kms_last_check, last_check, itv_rent, insurance) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehiculo.getNumplate());
            stmt.setString(2, vehiculo.getVehicle());
            stmt.setString(3, vehiculo.getDestination());
            stmt.setInt(4, vehiculo.getType().ordinal());
            stmt.setInt(5, vehiculo.getStatus().ordinal());
            stmt.setString(6, vehiculo.getAllocation());
            /*Object porque si no, no se come el nulo*/
            stmt.setObject(7, vehiculo.getKms_last_check());
            stmt.setString(8, vehiculo.getLast_check());
            stmt.setString(9, vehiculo.getItv_rent());
            stmt.setString(10, vehiculo.getInsurance());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el vehículo, ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehiculo.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del vehículo insertado.");
                }
            }
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }
        return vehiculo;
    }

    @Override
    public Traveller modify(Traveller vehiculo) throws SQLException {
        String sql = "UPDATE vehiculos SET numplate = ?, vehicle = ?, destination = ?, "
                + "type = ?, status = ?, allocation = ?, kms_last_check = ?, last_check = ?, "
                + "itv_rent = ?, insurance = ? WHERE id = ?";
        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehiculo.getNumplate());
            stmt.setString(2, vehiculo.getVehicle());
            stmt.setString(3, vehiculo.getDestination());
            stmt.setInt(4, vehiculo.getType().ordinal());
            stmt.setInt(5, vehiculo.getStatus().ordinal());
            stmt.setString(6, vehiculo.getAllocation());
            stmt.setObject(7, vehiculo.getKms_last_check());
            stmt.setString(8, vehiculo.getLast_check());
            stmt.setString(9, vehiculo.getItv_rent());
            stmt.setString(10, vehiculo.getInsurance());
            stmt.setLong(11, vehiculo.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el vehículo,"
                        + " ninguna fila afectada.");
            }
        } catch (Exception ex) {
            throw new SQLException(ex.getMessage());
        }
        return vehiculo;
    }

    @Override
    public boolean delete(Traveller entity) throws SQLException {
        String sql = "DELETE FROM vehiculos WHERE id = ?";
        try (Connection conn = SQLiteDataBase.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, entity.getId());
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (Exception ex) {
            throw new SQLException("No se pudo eliminar el vehiculo " + entity.getNumplate());
        }
    }

    @Override
    public List<Traveller> findAll() throws SQLException {
        List<Traveller> listV = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos";

        try (Connection conn = SQLiteDataBase.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Traveller v = new Traveller();
                v.setId(rs.getInt("id"));
                v.setNumplate(rs.getString("numplate"));
                v.setVehicle(rs.getString("vehicle"));
                v.setDestination(rs.getString("destination"));
                v.setType(rs.getInt("type"));
                v.setStatus(rs.getInt("status"));
                v.setAllocation(rs.getString("allocation"));
                int kms = rs.getInt("kms_last_check");
                if (rs.wasNull()) {
                    v.setKms_last_check(null);
                } else {
                    v.setKms_last_check(kms);
                }
                v.setLast_check(rs.getString("last_check"));
                v.setItv_rent(rs.getString("itv_rent"));
                v.setInsurance(rs.getString("insurance"));
                listV.add(v);
            }
        } catch (Exception ex) {
            throw new SQLException("No se pudo recoger la lista de vehiculos.");
        }

        return listV;
    }
}
