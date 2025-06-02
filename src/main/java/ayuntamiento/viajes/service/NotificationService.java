package ayuntamiento.viajes.service;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import ayuntamiento.viajes.model.Notification;
import ayuntamiento.viajes.model.Vehicle;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de las notificaciones que se encarga
 * de mostrar al usuario las notificaciones en la tabla de la vista
 * 
 * @author Ramón Iglesias Granados
 * @since 2025-05-12
 * @version 1.2
 */
public class NotificationService {

    private final static VehicleService vehicleS;
    private static final List<Notification> notificationsList;
    private static List<Vehicle> vehiclesList;

    static {
        vehicleS = new VehicleService();
        notificationsList = new ArrayList<>();
    }

    public NotificationService() {
    }
    
    public void rechargeNotifications() {
        notificationsList.clear();
        vehiclesList = vehicleS.findByWarning();
        for (Vehicle vehicle : vehiclesList) {
            String warning = getWarning(vehicle.getItv_RentDate(), vehicle.getInsuranceDate(), vehicle.getLast_CheckDate());
            notificationsList.add(new Notification(vehicle.getNumplate(),
                    vehicle.getVehicle(),
                    vehicle.getType(), warning, vehicle.getStatus()));

        }
    }

    public List<Notification> getNotificationsList() {
        return notificationsList;
    }

    public int getNumberOfNotifications() {
        return notificationsList.size();
    }

    private String getWarning(LocalDate itvDate, LocalDate insuranceDate, LocalDate lastCheckDate) {
        String itv_rentMsg = itvWarning(itvDate);
        String insuranceMsg = insuranceWarning(insuranceDate);
        String lastCheckMsg = lastCheckWarning(lastCheckDate);

        List<String> messages = new ArrayList<>();
        if (itv_rentMsg != null) {
            messages.add(itv_rentMsg);
        }
        if (insuranceMsg != null) {
            messages.add(insuranceMsg);
        }
        if (lastCheckMsg != null) {
            messages.add(lastCheckMsg);
        }

        if (messages.isEmpty()) {
            return null;
        } else if (messages.size() == 1) {
            return messages.get(0);
        } else if (messages.size() == 2) {
            return messages.get(0) + " y " + messages.get(1);
        } else {
            // Para 3 mensajes: separa con coma y " y " antes del último
            return messages.get(0) + ", " + messages.get(1) + " y " + messages.get(2);
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

    private String lastCheckWarning(LocalDate lastCheckDate) {
        LocalDate today = LocalDate.now();

        if (lastCheckDate.isAfter(today)) {
            return "Revisión Programada";
        }

        long daysUntilExpiration = ChronoUnit.DAYS.between(lastCheckDate, today);
                
        if (daysUntilExpiration >= 730) {
            return "La última Revisión fue hace dos años o más";

        } else if (daysUntilExpiration >= 365) {
            return "La última Revisión fue hace un año";
        } else {
            return null; // Más de 6 meses
        }
    }

}
