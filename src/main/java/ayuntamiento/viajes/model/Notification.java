package ayuntamiento.viajes.model;

import ayuntamiento.viajes.model.Vehicle.VehicleStatus;

/**
 * Clase entidad que se utiliza para guardar 
 * las notificaciones sobre los coches sin ITV
 * Alquiler o Seguro
 * 
 * @author Ramón Iglesias
 * @since 2025-05-12
 * @version 1.1
 */
public class Notification {
    
    private String numberplate;
    private String brand;
    private String model;
    private Vehicle.VehicleStatus type;
    private String warning;

    public Notification() {}

    public Notification(String numberplate, String brand, String model, int type, String warning) {
        this.numberplate = numberplate;
        this.brand = brand;
        this.model = model;
        this.type = VehicleStatus.values()[type];
        this.warning = warning;
    }

    
    public String getNumberplate() { return numberplate; }
    public void setNumberplate(String matricula) { this.numberplate = matricula; }

    public String getBrand() { return brand; }
    public void setBrand(String marca) { this.brand = marca; }

    public String getModel() { return model; }
    public void setModel(String modelo) { this.model = modelo; }

    public VehicleStatus getType() { return type; }
    public void setType(int type) { this.type = VehicleStatus.values()[type]; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }


}


