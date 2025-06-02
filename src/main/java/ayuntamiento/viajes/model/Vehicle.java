package ayuntamiento.viajes.model;

import ayuntamiento.viajes.common.PropertiesUtil;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Clase entidad que se utiliza para guardar los datos de los vehiculos
 *
 * @author Ramón Iglesias
 * @since 2025-05-09
 * @version 1.2
 */
public class Vehicle {

    private long id;
    private String numplate;
    private String vehicle;
    private String destination;
    private VehicleType type;
    private VehicleStatus status;
    private String allocation;
    private Integer kms_last_check;
    private LocalDate last_check;
    private LocalDate itv_rent;
    private LocalDate insurance;

    public enum VehicleType {
        Propiedad,
        Alquiler,
        Otro
    }

    public enum VehicleStatus {
        Buen_Estado,
        Mal_Estado,
        Averiado,
        Reparado,
        Fuera_de_Servicio,
        Otro;

        @Override
        public String toString() {
            return name().replace('_', ' ');
        }
    }

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    // Constructores
    public Vehicle() {
    }

    public Vehicle(String numplate, String vehicle, String destination, Integer type, Integer status,
            String allocation, Integer kms_last_check, String last_check, String itv_rentString, String insuranceString) {
        this.numplate = numplate;
        this.vehicle = vehicle;
        this.destination = (destination == null ? "" : destination);
        this.type = VehicleType.values()[type];
        this.status = VehicleStatus.values()[status];
        this.allocation = (allocation == null ? "" : allocation);
        this.kms_last_check = kms_last_check;
        this.last_check = (last_check != null && !last_check.isBlank()) ? parseITV_Rent_LastCheck(last_check) : null;
        this.itv_rent = (itv_rentString != null && !itv_rentString.isBlank()) ? parseITV_Rent_LastCheck(itv_rentString) : null;
        this.insurance = (insuranceString != null && !insuranceString.isBlank()) ? parseInsurance(insuranceString) : null;

    }

    public VehicleType getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = VehicleType.values()[type];
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumplate() {
        return numplate;
    }

    public void setNumplate(String numplate) {
        this.numplate = numplate;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = (destination == null ? "" : destination);
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = VehicleStatus.values()[status];
    }

    public String getAllocation() {
        return allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = (allocation == null ? "" : allocation);
    }

    public Integer getKms_last_check() {
        return kms_last_check;
    }

    public void setKms_last_check(Integer kms_last_check) {
        this.kms_last_check = kms_last_check;
    }

    public String getLast_check() {
        return last_check != null ? last_check.format(formatter_Show_Date) : null;
    }

    public void setLast_check(String last_checkString) {
        this.last_check = parseITV_Rent_LastCheck(last_checkString);
    }

    public LocalDate getLast_CheckDate() {
        return last_check;
    }

    public void setLast_CheckDate(LocalDate last_check) {
        this.last_check = last_check;
    }

    public LocalDate getItv_RentDate() {
        return itv_rent;
    }

    public void setItv_RentDate(LocalDate itv) {
        this.itv_rent = itv;
    }

    public String getItv_rent() {
        return itv_rent != null ? itv_rent.format(formatter_Show_Date) : null;
    }

    public void setItv_rent(String itvString) {
        this.itv_rent = parseITV_Rent_LastCheck(itvString);
    }

    public LocalDate getInsuranceDate() {
        return this.insurance;
    }

    public void setInsuranceDate(LocalDate insurance) {
        this.insurance = insurance;
    }

    public String getInsurance() {
        return insurance != null ? insurance.format(formatter_Show_Date) : null;
    }

    public void setInsurance(String dateString) {
        this.insurance = parseInsurance(dateString);
    }

    private LocalDate parseITV_Rent_LastCheck(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        YearMonth ym = YearMonth.parse(dateString, formatter_Show_Date);
        return ym.atEndOfMonth();
    }

    private LocalDate parseInsurance(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateString, formatter_Show_Date);
    }

}
