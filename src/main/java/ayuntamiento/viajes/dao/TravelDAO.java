package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Travel;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los viajes
 *
 * @author Ramón Iglesias Granados
 * @since 2025-08-20
 * @version 1.0
 */
public class TravelDAO extends APIClient {
    
    public TravelDAO(){
        super(Travel.class, "travels");
    }
}
