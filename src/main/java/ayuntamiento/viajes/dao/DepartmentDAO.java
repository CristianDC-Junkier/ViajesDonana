package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Department;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los departamentos
 *
 * @author Ramón Iglesias Granados
 * @since 2025-08-20
 * @version 1.0
 */
public class DepartmentDAO extends APIClient {
    
    public DepartmentDAO(){
        super(Department.class, "departments");
    }
}
