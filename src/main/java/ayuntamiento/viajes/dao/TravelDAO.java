package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.model.Travel;

/**
 *
 * @author Ramón Iglesias
 */
public class TravelDAO extends APIClient {
    
    public TravelDAO(){
        super(Travel.class, "travels");
    }
}
