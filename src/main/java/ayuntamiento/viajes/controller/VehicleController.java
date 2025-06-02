package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.PropertiesUtil;
import static ayuntamiento.viajes.controller.BaseController.refreshTable;
import ayuntamiento.viajes.exception.ControledException;
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
import ayuntamiento.viajes.model.Vehicle.VehicleType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.LocalDateStringConverter;

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias Granados
 * @since 2025-05-09
 * @version 1.7
 */
public class VehicleController extends BaseController implements Initializable {

    private final VehicleService vehicleS;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);


    public VehicleController() {
        vehicleS = new VehicleService();
    }

    @FXML
    private TableView<Vehicle> vehicleTable;
    @FXML
    private TableColumn numplateColumn;
    @FXML
    private TableColumn vehicleColumn;
    @FXML
    private TableColumn destinationColumn;
    @FXML
    private TableColumn typeColumn;
    @FXML
    private TableColumn statusColumn;
    @FXML
    private TableColumn allocationColumn;
    @FXML
    private TableColumn kms_last_checkColumn;
    @FXML
    private TableColumn last_checkColumn;
    @FXML
    private TableColumn itv_rentColumn;
    @FXML
    private TableColumn insuranceColumn;

    @FXML
    private Label amount;

    @FXML
    private TextField numplateTF;
    @FXML
    private TextField vehicleTF;
    @FXML
    private TextField destinationTF;

    @FXML
    private ChoiceBox statusCB;
    @FXML
    private ChoiceBox typeCB;

    @FXML
    private TextField km_lastCheckTF;
    @FXML
    private DatePicker lastCheckDP;

    @FXML
    private DatePicker itv_rentDP;
    @FXML
    private DatePicker insuranceDP;

    @FXML
    private void add() {
        showActionDialog(0);
    }

    @FXML
    private void modify() {
        showActionDialog(1);
    }

    /**
     * Metodo que elimina un vehículo, controla que el vehículo exista
     * preguntando si de verdad quieres hacerlo, colocando el field en rojo si
     * algo falla
     */
    @FXML
    private void delete() {
        if (vehicleTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un vehículo de la tabla",
                    "VehicleController - delete"));
        } else {
            try {
                if (info("¿Está seguro de que quiere eliminar este vehículo?", true) == InfoController.DialogResult.ACCEPT) {
                    if (vehicleS.delete(vehicleTable.getSelectionModel().getSelectedItem())) {
                        info("El vehículo fue eliminado con éxito", false);
                        refreshTable(vehicleTable, vehicleS.findAll());
                    } else {
                        throw new Exception("El vehículo no pudo ser borrado");
                    }
                }
            } catch (Exception ex) {
                error(ex);
            }
        }
    }

    /**
     * Este metodo sirve para colocar y descolocar las columnas de destination y
     * allocation
     */
    @FXML
    private void destinationTBChange() {
        destinationColumn.setVisible(!destinationColumn.isVisible());
        allocationColumn.setVisible(!allocationColumn.isVisible());
        destinationTF.setDisable(!destinationTF.isDisable());

        destinationColumn.setResizable(destinationColumn.isVisible());
        allocationColumn.setResizable(allocationColumn.isVisible());

        if (!destinationColumn.isVisible()) {
            destinationTF.setText("");
        }
    }

    /**
     * Este metodo sirve para colocar y descolocar las columnas de
     * last_checkColumn y km_lastCheckTF
     */
    @FXML
    private void lastCheckTBChange() {
        last_checkColumn.setVisible(!last_checkColumn.isVisible());
        kms_last_checkColumn.setVisible(!kms_last_checkColumn.isVisible());
        lastCheckDP.setDisable(!lastCheckDP.isDisable());
        km_lastCheckTF.setDisable(!km_lastCheckTF.isDisable());

        last_checkColumn.setResizable(last_checkColumn.isVisible());
        kms_last_checkColumn.setResizable(kms_last_checkColumn.isVisible());

        if (!last_checkColumn.isVisible()) {
            lastCheckDP.setValue(null);
            km_lastCheckTF.setText("");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        numplateColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("numplate"));
        vehicleColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("vehicle"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("destination"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, VehicleType>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, VehicleStatus>("status"));
        allocationColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, String>("allocation"));
        kms_last_checkColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, Integer>("kms_last_check"));
        last_checkColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, LocalDate>("last_check"));
        itv_rentColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, LocalDate>("itv_rent"));
        insuranceColumn.setCellValueFactory(new PropertyValueFactory<Vehicle, LocalDate>("insurance"));

        vehicleTable.setPlaceholder(new Label("No existen vehículos"));

        // Filtro de titularidad
        typeCB.getItems().add("Todos");
        for (VehicleType type : VehicleType.values()) {
            typeCB.getItems().add(type.toString());
        }
        typeCB.getSelectionModel().selectFirst();
        typeCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // Filtro de estado
        statusCB.getItems().add("Todos");
        for (VehicleStatus status : VehicleStatus.values()) {
            statusCB.getItems().add(status.toString());
        }
        statusCB.getSelectionModel().selectFirst();
        statusCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // Filtro de DatePicker
        itv_rentDP.setShowWeekNumbers(false);
        itv_rentDP.setConverter(new LocalDateStringConverter(formatter_Show_Date, null));

        insuranceDP.setShowWeekNumbers(false);
        insuranceDP.setConverter(new LocalDateStringConverter(formatter_Show_Date, null));

        lastCheckDP.setShowWeekNumbers(false);
        lastCheckDP.setConverter(new LocalDateStringConverter(formatter_Show_Date, null));

        km_lastCheckTF.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        vehicleTable.setItems(FXCollections.observableList(vehicleS.findAll()));

        vehicleTable.setPlaceholder(new Label("No existen vehículos"));
        amount.setText("Vehículos en Total: " + vehicleS.findAll().size());

    }

    /**
     * Este metodo sirve para llamar a ActionVehicleController, donde mode es si
     * se pulso el botón de añadir(0) o modificar(1), en caso de que fuera
     * modificar se debe haber seleccionado un vehiculo
     *
     * @param mode comprueba si fue llamado desde añadir(0) o modificar(1)
     */
    public void showActionDialog(int mode) {
        if (mode == 1 && vehicleTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un vehículo de la tabla",
                    "VehicleController - showActionDialog"));
        } else {
            try {
                Vehicle vResult = ActionVehicleController.showActionVehicle((Stage) father.getScene().getWindow(),
                        vehicleTable.getSelectionModel().getSelectedItem(), mode);
                if (vResult != null) {
                    if (mode == 0) {
                        anadir(vResult);
                        info("El Vehículo fue añadido correctamente", false);
                    } else {
                        modificar(vResult);
                        info("El Vehículo fue modificado correctamente", false);
                    }
                    refreshTable(vehicleTable, vehicleS.findAll());
                }
            } catch (ControledException ex) {
                error(ex);
            } catch (Exception ex) {
                error(ex);
            }
        }
    }

    public void anadir(Vehicle vehicle) throws Exception {
        if (vehicleS.save(vehicle) == null) {
            throw new ControledException("La matricula introducida ya existe: " + vehicle.getNumplate(),
                    "VehicleController - anadir");
        }
        refreshTable(vehicleTable, vehicleS.findAll());
    }

    public void modificar(Vehicle vehicle) throws Exception {
        if (vehicleS.modify(vehicle) == null) {
            throw new ControledException("La matricula introducida ya existe: " + vehicle.getNumplate(),
                    "VehicleController - anadir");
        }
        refreshTable(vehicleTable, vehicleS.findAll());
    }

    @FXML
    private void applyAllFilters() {
        String vehicleText = vehicleTF.getText() != null ? vehicleTF.getText().toLowerCase().trim() : "";
        String plateText = numplateTF.getText() != null ? numplateTF.getText().toLowerCase().trim() : "";
        String destinationText = destinationTF.getText() != null ? destinationTF.getText().toLowerCase().trim() : "";

        String selectedType = typeCB.getValue().toString();
        String selectedStatus = statusCB.getValue().toString();

        Integer kms = km_lastCheckTF.getText().isBlank() || km_lastCheckTF.getText() == null ? null : Integer.valueOf(km_lastCheckTF.getText().trim());
        LocalDate selectedLastCheckDate = lastCheckDP.getValue();

        LocalDate selectedItvDate = itv_rentDP.getValue();
        LocalDate selectedInsuranceDate = insuranceDP.getValue();

        List<Vehicle> filtered = vehicleS.findAll().stream()
                .filter(v
                        -> (vehicleText.isEmpty() || (v.getVehicle() != null && v.getVehicle().toLowerCase().contains(vehicleText)))
                && (plateText.isEmpty() || (v.getNumplate() != null && v.getNumplate().toLowerCase().contains(plateText)))
                && (destinationText.isEmpty() || (v.getDestination() != null && v.getDestination().toLowerCase().contains(destinationText)))
                && (selectedType.equals("Todos") || (v.getType() != null && v.getType().toString().equalsIgnoreCase(selectedType)))
                && (selectedStatus.equals("Todos") || (v.getStatus() != null && v.getStatus().toString().equalsIgnoreCase(selectedStatus)))
                && (kms == null || (v.getKms_last_check() != null && v.getKms_last_check() <= kms))
                && (selectedLastCheckDate == null || (v.getLast_CheckDate() != null && v.getLast_CheckDate().isBefore(selectedLastCheckDate)))
                && (selectedItvDate == null || (v.getItv_RentDate() != null && v.getItv_RentDate().isBefore(selectedItvDate)))
                && (selectedInsuranceDate == null || (v.getInsuranceDate() != null && v.getInsuranceDate().isBefore(selectedInsuranceDate)))
                )
                .collect(Collectors.toList());

        refreshTable(vehicleTable, filtered);
    }

    @FXML
    private void resetAllFilters() {
        vehicleTF.setText("");
        numplateTF.setText("");
        destinationTF.setText("");
        km_lastCheckTF.setText("");

        typeCB.setValue(typeCB.getItems().get(0));
        statusCB.setValue(statusCB.getItems().get(0));
        
        lastCheckDP.setValue(null);
        itv_rentDP.setValue(null);
        insuranceDP.setValue(null);
        
        applyAllFilters();

    }

}
