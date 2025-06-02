package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.TravellerDAO;
import ayuntamiento.viajes.model.Traveller;
import java.sql.SQLException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que se encarga de dar a los controladores acceso a los vehiculos siendo
 * el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-12
 * @version 1.4
 */
public class TravellerService {

    private static final TravellerDAO travellerDAO;
    private static List<Traveller> travellerList;

    static {
        travellerDAO = new TravellerDAO();
    }

    public Traveller save(Traveller entity) throws SQLException {
        Traveller result;
        boolean vehicleExists = travellerList.stream()
                .anyMatch(vehicle -> vehicle.getNumplate().equalsIgnoreCase(entity.getNumplate()));
        if (vehicleExists) {
            result = null;
        } else {
            result = travellerDAO.save(entity);
            rechargeList();
        }
        return result;
    }

    public Traveller modify(Traveller entity) throws SQLException {
        Traveller result;
        boolean vehicleExists = travellerList.stream()
                .anyMatch(vehicle -> vehicle.getNumplate().equalsIgnoreCase(entity.getNumplate())
                && vehicle.getId() != entity.getId());
        if (vehicleExists) {
            result = null;
        } else {
            result = travellerDAO.modify(entity);
            rechargeList();
        }
        return result;
    }

    public boolean delete(Traveller entity) throws SQLException {
        boolean deleted;
        deleted = travellerDAO.delete(entity);
        if (deleted) {
            rechargeList();
        }
        return deleted;
    }

    public List<Traveller> findAll() {
        return travellerList;
    }

    public List<Traveller> findByTrip(int trip) {
        return travellerList.stream()
                .filter(v -> v.getType().ordinal() == trip)
                .collect(Collectors.toList());
    }

    public List<Traveller> findByOffice(int office) {
        return travellerList.stream()
                .filter(v -> v.getType().ordinal() == office)
                .collect(Collectors.toList());
    }

    public void rechargeList() throws SQLException {
        travellerList = travellerDAO.findAll();

    }

}
