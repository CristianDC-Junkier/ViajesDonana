package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Traveller;


/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos de los travellers
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class TravellerDAO extends APIClient  {

    public TravellerDAO() {
        super(Traveller.class, "travellers");
    }


}
