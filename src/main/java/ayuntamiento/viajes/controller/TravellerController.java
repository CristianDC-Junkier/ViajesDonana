package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ChoiceBoxUtil;
import ayuntamiento.viajes.common.PropertiesUtil;
import static ayuntamiento.viajes.controller.BaseController.refreshTable;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
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

import ayuntamiento.viajes.service.TravellerService;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.LoginService;
import ayuntamiento.viajes.service.TravelService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.converter.LocalDateStringConverter;

/**
 * Clase Controladora que se encarga de gestionar los travellers existentes
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
    private TableColumn signupColumn;

    @FXML
    private Label amount;

    @FXML
    private TextField dniTF;
    @FXML
    private TextField nameTF;

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
                        Travel t = travelS.findById(travellerTable.getSelectionModel().getSelectedItem().getTrip()).get();
                        t.removeTraveller();
                        travelS.modify(t);
                        refreshTable(travellerTable, travellerS.findAll());
                    } else {
                        throw new Exception("El viajero no pudo ser borrado");
                    }
                }
            } catch (Exception ex) {
                refreshTable(travellerTable, travellerS.findAll());
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
            public ObservableValue<String> call(CellDataFeatures<Traveller, String> p) {
                return new SimpleStringProperty(departmentS.findById(p.getValue().getDepartment()).get().getName().replace('_', ' '));
            }
        });
        tripColumn.setCellValueFactory(new Callback<CellDataFeatures<Traveller, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Traveller, String> p) {
                return new SimpleStringProperty(travelS.findById(p.getValue().getTrip()).get().getDescriptor());
            }
        });
        signupColumn.setCellValueFactory(new PropertyValueFactory<Traveller, LocalDate>("signup"));

        if (LoginService.getAdminDepartment().getName().equalsIgnoreCase("Admin")) {
            Department allDepartment = new Department();
            allDepartment.setId(0);
            allDepartment.setName("Todos");
            departmentCB.getItems().add(allDepartment);

            // Carga departamentos desde DepartmentService
            List<Department> departments = departmentS.findAll();
            departmentCB.getItems().addAll(departments);
            departmentCB.setValue(departmentCB.getItems().get(0));
        } else {
            departmentCB.getItems().add(LoginService.getAdminDepartment());
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
     * si se pulso el botón de añadir(0) o modificar(1), en caso de que fuera
     * modificar se debe haber seleccionado un vehiculo
     *
     * @param mode comprueba si fue llamado desde añadir(0) o modificar(1)
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
                        anadir(vResult);
                        info("El Viajero fue añadido correctamente", false);
                    } else {
                        modificar(vResult);
                        info("El Viajero fue modificado correctamente", false);
                    }
                    refreshTable(travellerTable, travellerS.findAll());
                }
            } catch (ControledException cE) {
                refreshTable(travellerTable, travellerS.findAll());
                error(cE);
            } catch (Exception ex) {
                error(ex);
            }
        }
    }

    public void anadir(Traveller entity) throws Exception {
        Travel t = travelS.findById(entity.getTrip()).get();
        if (t.getSeats_occupied() < t.getSeats_total()) {
            if (travellerS.save(entity) == null) {
                refreshTable(travellerTable, travellerS.findAll());
                throw new ControledException("El DNI introducido ya existe: " + entity.getDni(),
                        "TravellerController - anadir");
            }
            t.addTraveller();
            travelS.modify(t);
        } else {
            throw new ControledException("El viaje seleccionado está completo: " + t.getDescriptor(),
                    "TravellerController - anadir");
        }
        refreshTable(travellerTable, travellerS.findAll());
    }

    public void modificar(Traveller entity) throws Exception {
        Travel t = travelS.findById(entity.getTrip()).get();
        Travel tt = travelS.findById(travellerS.findById(entity.getId()).getTrip()).get();
        if (t != tt) {
            if (t.getSeats_occupied() < t.getSeats_total()) {
                if (travellerS.modify(entity) == null) {
                    refreshTable(travellerTable, travellerS.findAll());
                    throw new ControledException("El DNI introducido ya existe: " + entity.getDni(),
                            "TravellerController - anadir");
                }
                t.addTraveller();
                tt.removeTraveller();
                travelS.modify(t);
                travelS.modify(tt);
            } else {
                throw new ControledException("El viaje seleccionado está completo: " + t.getDescriptor(),
                        "TravellerController - anadir");
            }
        } else {
            if (travellerS.modify(entity) == null) {
                refreshTable(travellerTable, travellerS.findAll());
                throw new ControledException("El DNI introducido ya existe: " + entity.getDni(),
                        "TravellerController - anadir");
            }
        }
        refreshTable(travellerTable, travellerS.findAll());
    }

    @FXML
    private void applyAllFilters() {
        String nameText = nameTF.getText() != null ? nameTF.getText().toLowerCase().trim() : "";
        String dniText = dniTF.getText() != null ? dniTF.getText().toLowerCase().trim() : "";

        long selectedDepartment = departmentCB.getValue().getId();
        long selectedTrip = tripCB.getValue().getId();

        LocalDate selectedInsuranceDate = sign_upDP.getValue();

        List<Traveller> filtered = travellerS.findAll().stream()
                .filter(t
                        -> (nameText.isEmpty() || (t.getName() != null && t.getName().toLowerCase().contains(nameText)))
                && (dniText.isEmpty() || (t.getDni() != null && t.getDni().toLowerCase().contains(dniText)))
                && (selectedDepartment == 0 || (t.getDepartment() == selectedDepartment))
                && (selectedTrip == 0 || (t.getTrip() == selectedTrip))
                && (selectedInsuranceDate == null || (t.getSignUpDate() != null && t.getSignUpDate().isBefore(selectedInsuranceDate)))
                )
                .collect(Collectors.toList());

        refreshTable(travellerTable, filtered);
    }

    @FXML
    private void resetAllFilters() {
        nameTF.setText("");
        dniTF.setText("");

        departmentCB.setValue(departmentCB.getItems().get(0));
        tripCB.setValue(tripCB.getItems().get(0));

        sign_upDP.setValue(null);

        applyAllFilters();

    }

}
