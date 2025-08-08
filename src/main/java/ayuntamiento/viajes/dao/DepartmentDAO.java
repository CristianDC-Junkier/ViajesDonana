package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Department;

/**
 *
 * @author Ramón Iglesias
 */
public class DepartmentDAO extends APIClient {
    
    public DepartmentDAO(){
        super(Department.class, "departments");
    }
}
