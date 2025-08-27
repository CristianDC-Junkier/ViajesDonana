package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Worker;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los administradores
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class WorkerDAO extends APIClient {

    public WorkerDAO() {
        super(Worker.class, "workers");
    }

}
