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
 * @since 2025-06-04
 * @version 1.2
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
                .anyMatch(vehicle -> vehicle.getDni().equalsIgnoreCase(entity.getDni()));
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
                .anyMatch(vehicle -> vehicle.getDni().equalsIgnoreCase(entity.getDni())
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
                .filter(v -> v.getTrip().ordinal() == trip)
                .collect(Collectors.toList());
    }

    public List<Traveller> findByOffice(int office) {
        return travellerList.stream()
                .filter(v -> v.getOffice().ordinal() == office)
                .collect(Collectors.toList());
    }

    public void rechargeList() throws SQLException {
        travellerList = travellerDAO.findAll();
        /*travellerList = List.of(
                new Traveller("12345678A", "Ana Pérez", 0, 0, "01/06/2023"),
                new Traveller("23456789B", "Luis Gómez", 1, 1, "01/06/2023"),
                new Traveller("34567890C", "María López", 2, 2, "20/07/2023"),
                new Traveller("45678901D", "Carlos Ruiz", 0, 3, "05/06/2023"),
                new Traveller("56789012E", "Laura Martín", 1, 4, "12/07/2023"),
                new Traveller("67890123F", "Javier Sánchez", 2, 5, "10/06/2023"),
                new Traveller("78901234G", "Sofía Torres", 0, 0, "20/08/2023"),
                new Traveller("89012345H", "Miguel Fernández", 1, 1, "30/09/2023"),
                new Traveller("90123456I", "Elena Díaz", 2, 2, "07/09/2023"),
                new Traveller("01234567J", "Pedro Morales", 0, 3, "19/06/2023"),
                new Traveller("11223344K", "Isabel Ruiz", 1, 4, "12/07/2023"),
                new Traveller("22334455L", "Raúl Jiménez", 2, 5, "28/06/2023")
                
        );*/

    }

}
