package ayuntamiento.viajes.model;

import ayuntamiento.viajes.common.PropertiesUtil;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Clase entidad que se utiliza para guardar 
 * los datos de los vehiculos
 * 
 * @author Ramón Iglesias
 * @since 2025-05-09
 * @version 1.0
 */
public class Vehicle {

    private long id;
    private String numplate;
    private String brand;
    private String model;
    private VehicleStatus type;
    private LocalDate itv_rent;
    private LocalDate insurance;
    
    public enum VehicleStatus {
        Propio,
        Alquilado,
        Prestado
    }

    private static final String ITV_RENT_DATE_FORMAT = PropertiesUtil.getProperty("ITV_RENT_DATE_FORMAT");
    private static final DateTimeFormatter formatter_ITV_Rent = DateTimeFormatter.ofPattern(ITV_RENT_DATE_FORMAT);
    private static final String INSURANCE_DATE_FORMAT = PropertiesUtil.getProperty("INSURANCE_DATE_FORMAT");
    private static final DateTimeFormatter formatter_INSURANCE = DateTimeFormatter.ofPattern(INSURANCE_DATE_FORMAT);

    // Constructores
    public Vehicle() {
    }

    public Vehicle(String nameplate, String brand, String model, int type, String itv_rentString, String insuranceString) {
        this.numplate = nameplate;
        this.brand = brand;
        this.model = model;
        this.type = VehicleStatus.values()[type];
        this.itv_rent = parseITV_Rent(itv_rentString);
        this.insurance = parseInsurance(insuranceString);
    }

    public VehicleStatus getType() { return type; }
    public void setType(int type) { this.type = VehicleStatus.values()[type]; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNumplate() { return numplate; }
    public void setNumplate(String numplate) { this.numplate = numplate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public LocalDate getItv_RentDate() { return itv_rent; }
    public void setItv_RentDate(LocalDate itv) { this.itv_rent = itv; }

    public String getItv_rent() { return itv_rent.format(formatter_ITV_Rent); }
    public void setItv_rent(String itvString) { this.itv_rent = parseITV_Rent(itvString); }
    
    public LocalDate getInsuranceDate() { return this.insurance; }
    public void setInsuranceDate(LocalDate insurance) { this.insurance = insurance; }
    
    public String getInsurance() { return insurance.format(formatter_INSURANCE); }
    public void setInsurance(String dateString) { this.insurance = parseInsurance(dateString); }

    
    private LocalDate parseITV_Rent(String dateString) {
        YearMonth ym = YearMonth.parse(dateString, formatter_ITV_Rent);
        return ym.atEndOfMonth();
    }

    private LocalDate parseInsurance(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateString, formatter_INSURANCE);
    }
}
