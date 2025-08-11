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
    private Department department;
    private Travel trip;
    private LocalDate signup;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    // Constructores
    public Traveller() {
    }

    public Traveller(String dni, String name, Department deparment, Travel trip, String sign_upString) {
        this.dni = dni;
        this.name = name;
        this.department = deparment;
        this.trip = trip;
        this.signup = (sign_upString != null && !sign_upString.isBlank()) ? parseSignUp(sign_upString) : null;

    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department deparment) {
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

    public Travel getTrip() {
        return trip;
    }
    public void setTrip(Travel trip) {
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

}
