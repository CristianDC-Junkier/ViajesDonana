package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Admin;
/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los administradores
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class AdminDAO extends APIClient {

    public AdminDAO() {
        super(Admin.class, "admins");
    }

}
