package ayuntamiento.viajes.model;

import ayuntamiento.viajes.model.Traveller.VehicleStatus;
import ayuntamiento.viajes.model.Traveller.VehicleType;

/**
 * Clase entidad que se utiliza para guardar 
 * las notificaciones sobre los coches con problemas con 
 * la ITV/Alquiler, el Seguro y las revisiones 
 * 
 * @author Ramón Iglesias Granados
 * @since 2025-05-12
 * @version 1.2
 */
public class Notification {
    
    private String numberplate;
    private String vehicle;
    private Traveller.VehicleType type;
    private Traveller.VehicleStatus status;
    private String warning;

    public Notification() {}

    public Notification(String numberplate, String vehicle, VehicleType type, String warning, VehicleStatus status) {
        this.numberplate = numberplate;
        this.vehicle = vehicle;
        this.type = type;
        this.status = status;
        this.warning = warning;
    }

    
    public String getNumberplate() { return numberplate; }
    public void setNumberplate(String matricula) { this.numberplate = matricula; }

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }

    public VehicleType getType() { return type; }
    public void setType(int type) { this.type = VehicleType.values()[type]; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    

}


