package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.PropertiesUtil;
import static ayuntamiento.viajes.controller.BaseController.refreshTable;
import ayuntamiento.viajes.model.Notification;
import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import ayuntamiento.viajes.service.VehicleService;
import ayuntamiento.viajes.model.Vehicle;
import ayuntamiento.viajes.model.Vehicle.VehicleStatus;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.converter.LocalDateStringConverter;

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class VehicleController extends BaseController implements Initializable {

    private final VehicleService vehicleS;

    private static final String ITV_RENT_DATE_FORMAT = PropertiesUtil.getProperty("ITV_RENT_DATE_FORMAT");
    private static final DateTimeFormatter dateformatterITV_Rent = DateTimeFormatter.ofPattern(ITV_RENT_DATE_FORMAT);
    private static final String INSURANCE_DATE_FORMAT = PropertiesUtil.getProperty("INSURANCE_DATE_FORMAT");
    private static final DateTimeFormatter dateformatterInsurance = DateTimeFormatter.ofPattern(INSURANCE_DATE_FORMAT);

    public VehicleController() {
        vehicleS = new VehicleService();
    }

    @FXML
    private TableView<Vehicle> vehicleTable;
    @FXML
    private TableColumn idColumn;
    @FXML
    private TableColumn numplateColumn;
    @FXML
    private TableColumn brandColumn;
    @FXML
    private TableColumn modelColumn;
    @FXML
    private TableColumn typeColumn;
    @FXML
    private TableColumn itv_rentColumn;
    @FXML
    private TableColumn insuranceColumn;

    @FXML
    private Label amount;
    @FXML
    private TextField brand;
    @FXML
    private TextField numplate;
    @FXML
    private ChoiceBox type;

    @FXML
    private DatePicker itv_rent;
    @FXML
    private DatePicker insurance;

    @FXML
    private void itv_rentChange() {
        applyAllFilters();
    }

    @FXML
    private void insuranceChange() {
        applyAllFilters();
    }

    @FXML
    private void nameplateChange() {
        applyAllFilters();
    }

    @FXML
    private void brandChange() {
        applyAllFilters();
    }

    @FXML
    private void add() {
        showActionDialog("Add");
    }

    @FXML
    private void modify() {
        showActionDialog("Mod");
    }

    @FXML
    private void delete() {
        if (vehicleTable.getSelectionModel().getSelectedItem() == null) {
            error("Debe seleccionar un vehiculo de la tabla");
        } else {
            vehicleS.delete(vehicleTable.getSelectionModel().getSelectedItem());
            refreshTable(vehicleTable, vehicleS.findAll());
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        idColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, Long>("id"));
        numplateColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("numplate"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("model"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, VehicleStatus>("type"));
        itv_rentColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, LocalDate>("itv_rent"));
        insuranceColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, LocalDate>("insurance"));

        vehicleTable.setPlaceholder(new Label("No existen vehículos"));

        // Filtro de tipo
        type.getItems().addAll("Todos", "Propio", "Alquilado", "Prestado");
        type.getSelectionModel().selectFirst();
        type.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // Filtro de DatePicker
        itv_rent.setShowWeekNumbers(false);
        itv_rent.setConverter(new LocalDateStringConverter(dateformatterITV_Rent, null));

        insurance.setShowWeekNumbers(false);
        insurance.setConverter(new LocalDateStringConverter(dateformatterInsurance, null));

        vehicleTable.setItems(FXCollections.observableList(vehicleS.findAll()));

        vehicleTable.setPlaceholder(new Label("No existen vehiculos"));
        amount.setText("Vehículos en Total: " + vehicleS.findAll().size());

    }

    public void showActionDialog(String mode) {
        ActionVehicleController.setMode(mode);
        if (mode == "Mod" && vehicleTable.getSelectionModel().getSelectedItem() == null) {
            error("Debe seleccionar un vehiculo de la tabla");
        } else {
            ActionVehicleController.setSelected(vehicleTable.getSelectionModel().getSelectedItem());
            ActionVehicleController.showActionVehicle((Stage) father.getScene().getWindow());

            if (mode == "Add") {
                anadir(ActionVehicleController.getResult());
            } else if (mode == "Mod") {
                modificar(ActionVehicleController.getResult());
            }
        }
    }

    public void anadir(Vehicle vehicle) {
        vehicleS.save(vehicle);
        refreshTable(vehicleTable, vehicleS.findAll());
    }

    public void modificar(Vehicle vehicle) {
        vehicleS.modify(vehicle);
        refreshTable(vehicleTable, vehicleS.findAll());
    }

    private void applyAllFilters() {
        String brandText = brand.getText().toLowerCase().trim();
        String plateText = numplate.getText().toLowerCase().trim();
        String selectedType = type.getValue() == null ? "Todos" : type.getValue().toString();
        LocalDate selectedItvDate = itv_rent.getValue();
        LocalDate selectedInsuranceDate = insurance.getValue();

        List<Vehicle> filtered = vehicleS.findAll().stream()
                .filter(v
                        -> (brandText.isEmpty() || (v.getBrand() != null && v.getBrand().toLowerCase().contains(brandText)))
                && (plateText.isEmpty() || (v.getNumplate() != null && v.getNumplate().toLowerCase().contains(plateText)))
                && (selectedType.equals("Todos") || (v.getType() != null && v.getType().toString().equalsIgnoreCase(selectedType)))
                && (selectedItvDate == null || (v.getItv_rent() != null && v.getItv_RentDate().isBefore(selectedItvDate)))
                && (selectedInsuranceDate == null || (v.getInsuranceDate() != null && v.getInsuranceDate().isBefore(selectedInsuranceDate)))
                )
                .collect(Collectors.toList());

        refreshTable(vehicleTable, filtered);
    }

}
