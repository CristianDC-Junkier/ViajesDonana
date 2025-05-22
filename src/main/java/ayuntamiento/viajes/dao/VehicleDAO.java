
package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los vehiculos
 * 
 * @author Ramón Iglesias
 * @since 2025-05-09
 * @version 1.0
 */
public class VehicleDAO implements IDao<Vehicle>{
      
    @Override
    public Vehicle save(Vehicle vehiculo) {
        String sql = "INSERT INTO vehiculos (nameplate, brand, model, type, itv, insurance) VALUES(?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = SQLiteDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehiculo.getNumplate());
            stmt.setString(2, vehiculo.getBrand());
            stmt.setString(3, vehiculo.getModel());
            stmt.setInt(4, vehiculo.getType().ordinal());
            stmt.setString(5, vehiculo.getItv_rent());
            stmt.setString(6, vehiculo.getInsurance());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el usuario, ninguna fila afectada.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                // Asigna el ID generado al objeto vehiculo
                if (generatedKeys.next()) {
                    vehiculo.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del usuario insertado.");
                }
            }
        }catch(SQLException s){
            System.out.println(s.toString());
        }
        return vehiculo;
    }

    @Override
    public Vehicle modify(Vehicle vehiculo) {
        String sql = "UPDATE vehiculos SET nameplate = ?, brand = ?, model = ?, type = ?, itv = ?, insurance = ? WHERE id = ?";
        try( Connection conn = SQLiteDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setString(1, vehiculo.getNumplate());
            stmt.setString(2, vehiculo.getBrand());
            stmt.setString(3, vehiculo.getModel());
            stmt.setInt(4, vehiculo.getType().ordinal());
            stmt.setString(5, vehiculo.getItv_rent());
            stmt.setString(6, vehiculo.getInsurance());
            stmt.setLong(7, vehiculo.getId());
            
            int affectedRows = stmt.executeUpdate();
            if(affectedRows == 0){
                throw new SQLException("No se pudo actualizar el usuario, ninguna fila afectada.");
            }
        }catch (SQLException e) {
            System.err.println("Las excepciones deben ser manejadas tambien a nivel superior VehicleDAO modify");
        }
        return vehiculo;
    }

    @Override
    public boolean delete(Vehicle entity) {
        String sql = "DELETE FROM vehiculos WHERE id = ?";
        try(Connection conn = SQLiteDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setLong(1, entity.getId());
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        }catch (SQLException e) {
            System.err.println("Las excepciones deben ser manejadas tambien a nivel superior VehicleDAO delete");
        }
        return false;
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT * FROM vehiculos WHERE id = ? LIMIT 1";
        try(Connection conn = SQLiteDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Vehicle v = new Vehicle(rs.getString("nameplate"),
                                        rs.getString("brand"),
                                        rs.getString("model"),
                                        rs.getInt("type"),
                                        rs.getString("itv"),
                                        rs.getString("insurance"));
                v.setId(rs.getLong("id"));
                return Optional.of(v);
            }
        }catch(SQLException e) {
            System.err.println("Las excepciones deben ser manejadas tambien a nivel superior");
        }
        return Optional.empty();
    }

    @Override
    public List<Vehicle> findAll() {
        List<Vehicle> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos";

        try (Connection conn = SQLiteDataBase.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vehicle v = new Vehicle();
                v.setId(rs.getInt("id"));
                v.setNumplate(rs.getString("nameplate"));
                v.setBrand(rs.getString("brand"));
                v.setModel(rs.getString("model"));
                v.setType(rs.getInt("type"));
                v.setItv_rent(rs.getString("itv"));
                v.setInsurance(rs.getString("insurance"));
                lista.add(v);
            }
        }catch(SQLException s){
            System.out.println("Las excepciones deben ser manejadas tambien a nivel superior VehicleDAO findAll");
        }

        return lista;
    }
}
