
package ayuntamiento.viajes.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz que se encarga de proveer metodos a los diferentes DAO
 * de control
 * 
 * @author Cristian
 * @since 2025-05-06
 * @version 1.2
 */
public interface IDao <T> {
    T save(T entity) throws SQLException;
    T modify(T entity) throws SQLException;
    boolean delete(T entity) throws SQLException;
        
    List<T> findAll() throws SQLException;
}