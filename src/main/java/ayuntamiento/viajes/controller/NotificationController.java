package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.model.Notification;
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
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class NotificationController extends BaseController implements Initializable {

    private final NotificationService notificationS;
    private final List<Notification> notifications;

    @FXML
    private TableView notificationTable;
    @FXML
    private TableColumn numplateColumn;
    @FXML
    private TableColumn brandColumn;
    @FXML
    private TableColumn modelColumn;
    @FXML
    private TableColumn typeColumn;
    @FXML
    private TableColumn warningColumn;

    @FXML
    private Label amount;
    @FXML
    private TextField numplate;
    @FXML
    private ChoiceBox type;

    public NotificationController() {
        notificationS = new NotificationService();
        notifications = notificationS.getNotificationsList();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        // Configurar columnas
        numplateColumn.setCellValueFactory(new PropertyValueFactory<>("numberplate"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        warningColumn.setCellValueFactory(new PropertyValueFactory<>("warning"));

        notificationTable.setItems(FXCollections.observableList(notifications));

        notificationTable.setPlaceholder(new Label("No existen notificaciones"));
        amount.setText("Total de Notificaciones: " + notificationS.getNumberOfNotifications());

        // Filtro de tipo
        type.getItems().addAll("Todos", "Propio", "Alquilado", "Prestado");
        type.getSelectionModel().selectFirst();

        type.valueProperty().addListener((obs, oldType, newType) -> typeFilter());
    }

    private void typeFilter() {
        String selectedType = (String) type.getValue();

        if (selectedType.equals("Todos")) {
            refreshTable(notificationTable, notifications);
        } else {
            List<Notification> filtered = notifications.stream()
                    .filter(n -> n.getType().toString().toLowerCase().equals(selectedType.toLowerCase()))
                    .collect(Collectors.toList());

            refreshTable(notificationTable, filtered);
        }
    }

    @FXML
    private void nameplateChange() {
        String plateText = numplate.getText().trim().toLowerCase();
        List<Notification> filtered = notifications.stream()
                .filter(n -> n.getNumberplate().toLowerCase().contains(plateText))
                .collect(Collectors.toList());

        refreshTable(notificationTable, filtered);
    }

}
