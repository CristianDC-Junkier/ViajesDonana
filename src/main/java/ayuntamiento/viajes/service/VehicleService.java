package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.VehicleDAO;
import ayuntamiento.viajes.model.Vehicle;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleService {

    private static final VehicleDAO vehicleDAO;
    private static List<Vehicle> vehicleList;

    // Bloque de inicialización estático para cargar los vehiculos solo una vez
    static {
        vehicleDAO = new VehicleDAO();
        vehicleList = vehicleDAO.findAll();

        vehicleList.add(new Vehicle("1234ABC", "Peugeot", "308", 1, "09/2026", "06/09/2030"));
        vehicleList.add(new Vehicle("5678DEF", "Ford", "Focus", 0, "03/2025", "15/08/2027"));
        vehicleList.add(new Vehicle("9012GHI", "Toyota", "Corolla", 2, "11/2024", "01/01/2026"));
        vehicleList.add(new Vehicle("3456JKL", "Renault", "Clio", 1, "07/2025", "20/11/2028"));
        vehicleList.add(new Vehicle("7890MNO", "Volkswagen", "Golf", 2, "05/2026", "10/10/2029"));
        vehicleList.add(new Vehicle("1122PQR", "Seat", "Ibiza", 0, "12/2023", "05/03/2025"));
        vehicleList.add(new Vehicle("3344STU", "Citroën", "C3", 0, "08/2027", "12/12/2031"));
        vehicleList.add(new Vehicle("5566VWX", "Hyundai", "i30", 1, "04/2025", "03/07/2026"));
        vehicleList.add(new Vehicle("7788YZA", "Kia", "Ceed", 0, "06/2024", "09/09/2025"));
        vehicleList.add(new Vehicle("9900BCD", "Nissan", "Micra", 1, "10/2026", "11/11/2029"));

    }

    public Vehicle save(Vehicle entity) {
        boolean vehicleExists = vehicleList.stream()
                .anyMatch(vehicle -> vehicle.getNumplate().equalsIgnoreCase(entity.getNumplate())); 
        if (vehicleExists) {
            return null; 
        }
        vehicleList.add(entity);
        vehicleDAO.save(entity);
        return entity;
    }

    public Vehicle modify(Vehicle entity) {
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

    public boolean delete(Vehicle entity) {
        boolean deleted = vehicleDAO.delete(entity);  
        if (deleted) {
            vehicleList.remove(entity);
        }
        return deleted;
    }

    public List<Vehicle> findAll() {
        return vehicleList;  
    }

    public List<Vehicle> findByType(int type) {
        return vehicleList.stream()
                .filter(v -> v.getType().ordinal() == type)
                .collect(Collectors.toList());
    }

    public List<Vehicle> findByWarning() {
        LocalDate today = LocalDate.now();

        return vehicleList.stream()
                .filter(v -> {
                    LocalDate itvDate = v.getItv_RentDate();
                    LocalDate seguroDate = v.getInsuranceDate(); 

                    boolean itvWarning = itvDate != null && ChronoUnit.DAYS.between(today, itvDate) <= 186;
                    boolean seguroWarning = seguroDate != null && ChronoUnit.DAYS.between(today, seguroDate) <= 186;

                    return itvWarning || seguroWarning;
                })
                .collect(Collectors.toList());
    }

    public void rechargeList() {
        vehicleList = vehicleDAO.findAll();
    }

}
