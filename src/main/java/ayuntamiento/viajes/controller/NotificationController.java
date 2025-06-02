package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Notification;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.NotificationService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador de las notificaciones que se encarga
 * de mostrar al usuario las notificaciones en la tabla de la vista
 * 
 * @author Ramón Iglesias Granados
 * @since 2025-05-12
 * @version 1.2
 */
public class NotificationController extends BaseController implements Initializable {

    private final NotificationService notificationS;
    private final List<Notification> notifications;

    @FXML
    private TableView notificationTable;
    @FXML
    private TableColumn numplateColumn;
    @FXML
    private TableColumn vehicleColumn;
    @FXML
    private TableColumn typeColumn;
    @FXML
    private TableColumn statusColumn;
    @FXML
    private TableColumn warningColumn;

    @FXML
    private Label amount;
    @FXML
    private TextField numplateTF;
    @FXML
    private ChoiceBox typeCB;
    @FXML
    private ChoiceBox statusCB;

    public NotificationController() {
        notificationS = new NotificationService();
        notifications = notificationS.getNotificationsList();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        /* Configuración de las columnas */
        numplateColumn.setCellValueFactory(new PropertyValueFactory<>("numberplate"));
        vehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicle"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        warningColumn.setCellValueFactory(new PropertyValueFactory<>("warning"));

        notificationTable.setItems(FXCollections.observableList(notifications));

        notificationTable.setPlaceholder(new Label("No existen notificaciones"));
        amount.setText("Total de Notificaciones: " + notificationS.getNumberOfNotifications());

        /* Filtro de tipo */
        typeCB.getItems().add("Todos");
        for (Traveller.VehicleType t : Traveller.VehicleType.values()) {
            typeCB.getItems().add(t.toString());
        }
        typeCB.getSelectionModel().selectFirst();
        
        typeCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        /* Filtro de estado */
        statusCB.getItems().add("Todos");
        for (Traveller.VehicleStatus t : Traveller.VehicleStatus.values()) {
            statusCB.getItems().add(t.toString());
        }
        statusCB.getSelectionModel().selectFirst();

        statusCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());
    }

    @FXML
    private void applyAllFilters() {
        String plateText = numplateTF.getText() != null ? numplateTF.getText().toLowerCase().trim() : "";
        String selectedType = typeCB.getValue() != null ? typeCB.getValue().toString() : "Todos";
        String selectedStatus = statusCB.getValue() != null ? statusCB.getValue().toString() : "Todos";

        List<Notification> filtered = notifications.stream()
                .filter(n -> plateText.isEmpty()
                || (n.getNumberplate() != null && n.getNumberplate().toLowerCase().contains(plateText)))
                .filter(n -> selectedType.equalsIgnoreCase("Todos")
                || (n.getType() != null && n.getType().toString().equalsIgnoreCase(selectedType)))
                .filter(n -> selectedStatus.equalsIgnoreCase("Todos")
                || (n.getStatus()!= null && n.getStatus().toString().equalsIgnoreCase(selectedStatus)))
                .collect(Collectors.toList());

        refreshTable(notificationTable, filtered);
    }
    
    @FXML
    private void resetAllFilters() {
        numplateTF.setText("");
        
        typeCB.setValue(typeCB.getItems().get(0));
        statusCB.setValue(typeCB.getItems().get(0));
        
        applyAllFilters();
    }

}
