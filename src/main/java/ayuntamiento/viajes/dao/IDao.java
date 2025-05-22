
package ayuntamiento.viajes.dao;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Cristian
 */
public interface IDao <T> {
    T save(T entity);
    T modify(T entity);
    boolean delete(T entity);
        
    Optional<T> findById(Long id);

    List<T> findAll();
}