package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.VehicleDAO;
import ayuntamiento.viajes.model.Traveller;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que se encarga de dar a los controladores acceso a los vehiculos
 * siendo el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-12
 * @version 1.4
 */
public class VehicleService {

    private static final VehicleDAO vehicleDAO;
    private static List<Traveller> vehicleList;

    static {
        vehicleDAO = new VehicleDAO();
    }

    public Traveller save(Traveller entity) throws SQLException {
        Traveller result;
        boolean vehicleExists = vehicleList.stream()
                .anyMatch(vehicle -> vehicle.getNumplate().equalsIgnoreCase(entity.getNumplate()));
        if (vehicleExists) {
            return null;
        }
        result = vehicleDAO.save(entity);
        vehicleList.add(result);
        return entity;
    }

    public Traveller modify(Traveller entity) throws SQLException {
        boolean vehicleExists = vehicleList.stream()
                .anyMatch(vehicle -> vehicle.getNumplate().equalsIgnoreCase(entity.getNumplate())
                && vehicle.getId() != entity.getId());
        if (vehicleExists) {
            return null;
        }
        vehicleDAO.modify(entity);
        for (int i = 0; i < vehicleList.size(); i++) {
            if (vehicleList.get(i).getId() == entity.getId()) {
                vehicleList.set(i, entity);
            }
        }
        return entity;
    }

    public boolean delete(Traveller entity) throws SQLException {
        boolean deleted;
        deleted = vehicleDAO.delete(entity);
        if (deleted) {
            vehicleList.remove(entity);
        }
        return deleted;
    }

    public List<Traveller> findAll() {
        return vehicleList;
    }

    public List<Traveller> findByType(int type) {
        return vehicleList.stream()
                .filter(v -> v.getType().ordinal() == type)
                .collect(Collectors.toList());
    }

    /**
     * Metodo que recoje todos los vehiculos con algun problema
     * o sobre el que haya que hacer algún aviso
     * 
     * @return la lista de vehiculos con algun aviso
     */
    public List<Traveller> findByWarning() {
        LocalDate today = LocalDate.now();

        return vehicleList.stream()
                .filter(v -> {
                    LocalDate itv_rentDate = v.getItv_RentDate();
                    LocalDate insuranceDate = v.getInsuranceDate();
                    LocalDate lastcheckDate = v.getLast_CheckDate();

                    boolean itv_rentWarning = itv_rentDate != null && 
                            ChronoUnit.DAYS.between(today, itv_rentDate) <= 186;
                    boolean insuranceWarning = insuranceDate != null && 
                            ChronoUnit.DAYS.between(today, insuranceDate) <= 186;
                    boolean lastcheckWarning = lastcheckDate != null && 
                            (ChronoUnit.DAYS.between(lastcheckDate,today) >= 365 
                            || ChronoUnit.DAYS.between(today,lastcheckDate) > 0);
                    return itv_rentWarning || insuranceWarning || lastcheckWarning;
                })
                .collect(Collectors.toList());
    }

    public void rechargeList() throws SQLException {
        vehicleList = vehicleDAO.findAll();

    }

}
