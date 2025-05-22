package ayuntamiento.viajes.service;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import ayuntamiento.viajes.model.Notification;
import ayuntamiento.viajes.model.Vehicle;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Ramón Iglesias
 */
public class NotificationService {

    private final static VehicleService vehicleS;
    private static List<Notification> notificationsList;
    private static List<Vehicle> vehiclesList;

    static {
        vehicleS = new VehicleService();
        notificationsList = new ArrayList<>();
    }

    public NotificationService() {
    }

    public void rechargeNotifications() {
        notificationsList = new ArrayList<>();
        vehiclesList = vehicleS.findByWarning();
        for (Vehicle vehicle : vehiclesList) {
            String warning = getWarning(vehicle.getItv_RentDate(), vehicle.getInsuranceDate());
            notificationsList.add(new Notification(vehicle.getNumplate(), 
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getType().ordinal(), warning));

        }
    }

    public List<Notification> getNotificationsList() {
        return notificationsList;
    }

    public int getNumberOfNotifications() {
        return notificationsList.size();
    }

    private String getWarning(LocalDate itvDate, LocalDate insuranceDate) {
        String itvMsg = itvWarning(itvDate);
        String insuranceMsg = insuranceWarning(insuranceDate);

        if (itvMsg != null && insuranceMsg != null) {
            return itvMsg + " y " + insuranceMsg;
        } else if (itvMsg != null) {
            return itvMsg;
        } else {
            return insuranceMsg; // puede ser null también
        }
    }

    private String itvWarning(LocalDate itvDate) {
        LocalDate today = LocalDate.now();

        if (itvDate.isBefore(today)) {
            return "ITV caducada";
        }

        long daysUntilExpiration = ChronoUnit.DAYS.between(today, itvDate);

        if (daysUntilExpiration <= 31) {
            return "La ITV vence en 1 mes o menos";
        } else if (daysUntilExpiration <= 93) { // 3 * 31
            return "La ITV vence en 3 meses o menos";
        } else if (daysUntilExpiration <= 186) { // 6 * 31
            return "La ITV vence en 6 meses o menos";
        } else {
            return null; // Más de 6 meses
        }
    }

    private String insuranceWarning(LocalDate insuranceDate) {
        LocalDate today = LocalDate.now();

        if (insuranceDate.isBefore(today)) {
            return "Seguro caducado";
        }

        long daysUntilExpiration = ChronoUnit.DAYS.between(today, insuranceDate);

        if (daysUntilExpiration <= 31) {
            return "El Seguro vence en 1 mes o menos";
        } else if (daysUntilExpiration <= 93) { // 3 * 31
            return "El Seguro vence en 3 meses o menos";
        } else if (daysUntilExpiration <= 186) { // 6 * 31
            return "El Seguro vence en 6 meses o menos";
        } else {
            return null; // Más de 6 meses
        }
    }

}
