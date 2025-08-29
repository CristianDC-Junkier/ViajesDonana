package ayuntamiento.viajes.model;

import ayuntamiento.viajes.common.PropertiesUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Clase entidad que se utiliza para guardar los datos de los viajeros
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-02
 * @version 1.2
 */
public class Traveller {

    private long id;
    private String dni;
    private String name;
    private int phone;
    private long department;
    private long trip;
    private LocalDate signup;
    private int version;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    // Constructores
    public Traveller() {
    }

    public Traveller(String dni, String name, int phone, long deparment, long trip, String sign_upString, int version) {
        this.dni = dni;
        this.name = name;
        this.phone = phone;
        this.department = deparment;
        this.trip = trip;
        this.signup = (sign_upString != null && !sign_upString.isBlank()) ? parseSignUp(sign_upString) : null;
        this.version = version;
    }

    public long getDepartment() {
        return department;
    }
    public void setDepartment(long deparment) {
        this.department = deparment;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPhone(){
        return phone;
    }
    public void setPhone(int phone){
        this.phone = phone;
    }

    public long getTrip() {
        return trip;
    }
    public void setTrip(long trip) {
        this.trip = trip;
    }

    public LocalDate getSignUpDate() {
        return this.signup;
    }
    public void setSignUpDate(LocalDate sign_up) {
        this.signup = sign_up;
    }
    public String getSignup() {
        return signup != null ? signup.format(formatter_Show_Date) : null;
    }
    public void setSignup(String dateString) {
        this.signup = parseSignUp(dateString);
    }

    private LocalDate parseSignUp(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateString, formatter_Show_Date);
    }
    
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }

}
