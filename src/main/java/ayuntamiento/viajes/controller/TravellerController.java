package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ChoiceBoxUtil;
import ayuntamiento.viajes.common.PropertiesUtil;
import static ayuntamiento.viajes.controller.BaseController.refreshTable;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.ReloadException;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.service.TravellerService;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.LoginService;
import ayuntamiento.viajes.service.TravelService;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.converter.LocalDateStringConverter;

/**
 * Clase Controladora que se encarga de gestionar los viajeros existentes
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-03
 * @version 1.3
 */
public class TravellerController extends BaseController implements Initializable {

    private final static TravellerService travellerS;
    public final static TravelService travelS;
    public final static DepartmentService departmentS;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    @FXML
    private TableView<Traveller> travellerTable;
    @FXML
    private TableColumn dniColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn departmentColumn;
    @FXML
    private TableColumn tripColumn;
    @FXML
    private TableColumn phoneColumn;
    @FXML
    private TableColumn signupColumn;

    @FXML
    private Label amount;

    @FXML
    private TextField dniTF;
    @FXML
    private TextField nameTF;
    @FXML
    private TextField phoneTF;

    @FXML
    private ChoiceBox<Travel> tripCB;
    @FXML
    private ChoiceBox<Department> departmentCB;
    @FXML
    private DatePicker sign_upDP;

    static {
        travellerS = new TravellerService();
        travelS = new TravelService();
        departmentS = new DepartmentService();
    }

    @FXML
    private void add() {
        showActionDialog(0);
    }

    @FXML
    private void modify() {
        showActionDialog(1);
    }

    /**
     * Metodo que elimina un traveller, controla que el traveller exista,
     * preguntando si de verdad quieres hacerlo, colocando el field en rojo si
     * algo falla
     */
    @FXML
    private void delete() {
        if (travellerTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un viajero de la tabla",
                    "TravellerController - delete"));
        } else {
            try {
                if (info("¿Está seguro de que quiere eliminar este viajero?", true) == InfoController.DialogResult.ACCEPT) {
                    if (travellerS.delete(travellerTable.getSelectionModel().getSelectedItem())) {
                        info("El viajero fue eliminado con éxito", false);
                        refreshTable(travellerTable, travellerS.findAll(), amount);
                    } else {
                        throw new Exception("El viajero no pudo ser borrado");
                    }
                }
            } catch (ControledException cE) {
                refreshTable(travellerTable, travellerS.findAll(), amount);
                error(cE);
            } catch (ReloadException rE) {
                if (rE.wasRecovered()) {
                    refreshTable(travellerTable, travellerS.findAll(), amount);
                }
                error(rE);
            } catch (Exception ex) {
                refreshTable(travellerTable, travellerS.findAll(), amount);
                error(ex);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        dniColumn.setCellValueFactory(new PropertyValueFactory<Traveller, String>("dni"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Traveller, String>("name"));
        //Callbacks para mostrar los departamentos y viajes por nombre en vez de por el ID
        departmentColumn.setCellValueFactory(new Callback<CellDataFeatures<Traveller, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Traveller, String> p) {
                String deptName = departmentS.findById(p.getValue().getDepartment())
                        .map(d -> d.getName().replace('_', ' '))
                        .orElse("");
                return new SimpleStringProperty(deptName);
            }
        });
        tripColumn.setCellValueFactory(new Callback<CellDataFeatures<Traveller, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Traveller, String> p) {
                return new SimpleStringProperty(travelS.findById(p.getValue().getTrip()).get().getDescriptor());
            }
        });
        phoneColumn.setCellValueFactory(new PropertyValueFactory<Traveller, Integer>("phone"));
        signupColumn.setCellValueFactory(new PropertyValueFactory<Traveller, LocalDate>("signup"));

        String role = LoginService.getAccountDepartmentLog().getName();
        if (role != null && (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Superadmin"))) {
            Department allDepartment = new Department();
            allDepartment.setId(0);
            allDepartment.setName("Todos");
            departmentCB.getItems().add(allDepartment);

            // Carga departamentos desde DepartmentService
            List<Department> departments = departmentS.findAll().stream()
                    .filter(d -> !d.getName().equalsIgnoreCase("Admin") && !d.getName().equalsIgnoreCase("Superadmin"))
                    .toList();

            departmentCB.getItems().addAll(departments);
            departmentCB.setValue(departments.get(0));
        } else {
            departmentCB.getItems().add(LoginService.getAccountDepartmentLog());
            departmentCB.setValue(departmentCB.getItems().get(0));
            departmentCB.setDisable(true);
            departmentCB.setMouseTransparent(true);
            ChoiceBoxUtil.setDisableArrow(departmentCB);
        }

        ChoiceBoxUtil.setDepartmentNameConverter(departmentCB);
        departmentCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        Travel allTravels = new Travel();
        allTravels.setId(0);
        allTravels.setDescriptor("Todos");
        allTravels.setBus(0);
        tripCB.getItems().add(allTravels);

        // Carga viajes desde TravelService
        List<Travel> travels = travelS.findAll();
        tripCB.getItems().addAll(travels);
        tripCB.setValue(travels.isEmpty() ? null : tripCB.getItems().get(0));

        ChoiceBoxUtil.setTravelConverter(tripCB);
        tripCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // Filtro de DatePicker
        sign_upDP.setShowWeekNumbers(false);
        sign_upDP.setConverter(new LocalDateStringConverter(formatter_Show_Date, null));

        travellerTable.setPlaceholder(new Label("No existen inscripciones"));

        if (travellerS.findAll() != null) {
            travellerTable.setItems(FXCollections.observableList(travellerS.findAll()));
            amount.setText("Inscripciones en Total: " + travellerS.findAll().size());
        }
    }

    /**
     * Este metodo sirve para llamar a ActionTravellerController, donde mode es
     * si se pulso el botón de addAction(0) o modifyAction(1), en caso de que
     * fuera modifyAction se debe haber seleccionado un vehiculo
     *
     * @param mode comprueba si fue llamado desde añadir(0) o modifyAction(1)
     */
    public void showActionDialog(int mode) {
        if (mode == 1 && travellerTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un viajero de la tabla",
                    "TravellerController - showActionDialog"));
        } else {
            try {
                Traveller vResult = ActionTravellerController.showActionTraveller((Stage) father.getScene().getWindow(),
                        travellerTable.getSelectionModel().getSelectedItem(), mode);
                if (vResult != null) {
                    if (mode == 0) {
                        addAction(vResult);
                        info("El Viajero fue añadido correctamente", false);
                    } else {
                        modifyAction(vResult);
                        info("El Viajero fue modificado correctamente", false);
                    }
                    refreshTable(travellerTable, travellerS.findAll(), amount);
                }
            } catch (ControledException cE) {
                refreshTable(travellerTable, travellerS.findAll(), amount);
                error(cE);
            } catch (ReloadException rE) {
                if (rE.wasRecovered()) {
                    refreshTable(travellerTable, travellerS.findAll(), amount);
                }
                error(rE);
            } catch (Exception ex) {
                error(ex);
            }
        }
    }

    public void addAction(Traveller entity) throws Exception {
        Travel t = travelS.findById(entity.getTrip()).get();
        if (t.getSeats_occupied() < t.getSeats_total()) {
            if (travellerS.save(entity) == null) {
                refreshTable(travellerTable, travellerS.findAll(), amount);
                throw new ControledException("El viajero con DNI/NIE: " + entity.getDni() + ", ya existe",
                        "TravellerController - anadir");
            }
        } else {
            throw new ControledException("El viaje seleccionado está completo: " + t.getDescriptor(),
                    "TravellerController - anadir");
        }
    }

    public void modifyAction(Traveller entity) throws Exception {
        Travel t = travelS.findById(entity.getTrip()).get();
        if (t.getSeats_occupied() < t.getSeats_total()) {
            if (travellerS.modify(entity) == null) {
                refreshTable(travellerTable, travellerS.findAll(), amount);
                throw new ControledException("El DNI/NIE: " + entity.getDni() + ", ya está registrado",
                        "TravellerController - anadir");
            }
        } else {
            throw new ControledException("El viaje seleccionado está completo: " + t.getDescriptor(),
                    "TravellerController - anadir");
        }
    }

    @FXML
    private void applyAllFilters() {
        String nameText = nameTF.getText() != null ? nameTF.getText().toLowerCase().trim() : "";
        String dniText = dniTF.getText() != null ? dniTF.getText().toLowerCase().trim() : "";
        String phoneText = phoneTF.getText() != null ? phoneTF.getText().toLowerCase().trim() : "";

        long selectedDepartment = departmentCB.getValue().getId();
        long selectedTrip = tripCB.getValue().getId();

        LocalDate selectedInsuranceDate = sign_upDP.getValue();

        List<Traveller> filtered = travellerS.findAll().stream()
                .filter(t
                        -> (nameText.isEmpty() || (t.getName() != null && t.getName().toLowerCase().contains(nameText)))
                && (dniText.isEmpty() || (t.getDni() != null && t.getDni().toLowerCase().contains(dniText)))
                && (selectedDepartment == 0 || (t.getDepartment() == selectedDepartment))
                && (selectedTrip == 0 || (t.getTrip() == selectedTrip))
                && (phoneText.isEmpty() || (t.getPhone().contains(phoneText)))
                && (selectedInsuranceDate == null || (t.getSignUpDate() != null && t.getSignUpDate().isBefore(selectedInsuranceDate)))
                )
                .collect(Collectors.toList());

        refreshTable(travellerTable, filtered, amount);
    }

    @FXML
    private void resetAllFilters() {
        nameTF.setText("");
        dniTF.setText("");
        phoneTF.setText("");

        departmentCB.setValue(departmentCB.getItems().get(0));
        tripCB.setValue(tripCB.getItems().get(0));

        sign_upDP.setValue(null);

        applyAllFilters();

    }

}
