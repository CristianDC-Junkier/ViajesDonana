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
import javafx.beans.property.SimpleObjectProperty;

import javafx.beans.property.SimpleStringProperty;

import javafx.scene.control.TableCell;
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
import javafx.scene.control.TextField;
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
    private TableColumn<Traveller, String> dniColumn;
    @FXML
    private TableColumn<Traveller, String> nameColumn;
    @FXML
    private TableColumn<Traveller, String> departmentColumn;
    @FXML
    private TableColumn<Traveller, String> tripColumn;
    @FXML
    private TableColumn<Traveller, String> phoneColumn;
    @FXML
    private TableColumn<Traveller, LocalDate> signupColumn;

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

        // --- Configuración de columnas ---
        // DNI 
        dniColumn.setCellValueFactory(cellData -> {
            String dni = cellData.getValue().getDni().toUpperCase();
            String displayValue;

            if (dni != null && dni.contains("-")) {
                displayValue = dni.substring(0, dni.indexOf("-")).trim().toUpperCase();
                boolean esDniNieValido = displayValue.matches("^[0-9]{8}[A-Za-z]$") // DNI
                        || displayValue.matches("^[XYZxyz][0-9]{7}[A-Za-z]$");       // NIE
                if (!esDniNieValido) {
                    displayValue = "Menor sin DNI";
                }
            } else {
                displayValue = dni;
            }

            return new SimpleStringProperty(displayValue);
        });

        // Nombre
        nameColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getName().toUpperCase();
            return new SimpleStringProperty(name);
        });

        // Departamento (mostrar nombre en lugar del ID)
        departmentColumn.setCellValueFactory(p -> {
            String deptName = departmentS.findById(p.getValue().getDepartment())
                    .map(d -> d.getName().replace('_', ' '))
                    .orElse("");
            return new SimpleStringProperty(deptName);
        });

        // Viaje (mostrar descripción del viaje)
        tripColumn.setCellValueFactory(p -> {
            String tripDesc = travelS.findById(p.getValue().getTrip())
                    .map(Travel::getDescriptor)
                    .orElse("");

            if (tripDesc != null && tripDesc.contains(" - ")) {
                tripDesc = tripDesc.substring(0, tripDesc.indexOf(" - "));
            }
            return new SimpleStringProperty(tripDesc);
        });

        // Comparator para ordenar por fecha
        tripColumn.setComparator((s1, s2) -> {
            LocalDate d1 = null, d2 = null;

            try {
                if (s1 != null && !s1.isEmpty()) {
                    d1 = LocalDate.parse(s1.split("-")[0], formatter_Show_Date);
                }
                if (s2 != null && !s2.isEmpty()) {
                    d2 = LocalDate.parse(s2.split("-")[0], formatter_Show_Date);
                }
            } catch (Exception e) {
                // Si hay error de parsing, los consideramos iguales
                return 0;
            }

            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return -1;
            }
            if (d2 == null) {
                return 1;
            }
            return d1.compareTo(d2);
        });

        // Teléfono
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Fecha de inscripción
        signupColumn.setCellValueFactory(cellData -> {
            String str = cellData.getValue().getSignup(); // llega como "dd/MM/yyyy"
            LocalDate date = null;
            if (str != null && !str.isEmpty()) {
                date = LocalDate.parse(str, formatter_Show_Date);
            }
            return new SimpleObjectProperty<>(date);
        });

        signupColumn.setCellFactory(column -> new TableCell<Traveller, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter_Show_Date));
            }
        });

        // --- Configuración de filtros y choiceboxes ---
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
            departmentCB.setValue(departmentCB.getItems().get(0));
        } else {
            departmentCB.getItems().add(LoginService.getAccountDepartmentLog());
            departmentCB.setValue(departmentCB.getItems().get(0));
            departmentCB.setDisable(true);
            departmentCB.setMouseTransparent(true);
            ChoiceBoxUtil.setDisableArrow(departmentCB);
        }

        ChoiceBoxUtil.setDepartmentNameConverter(departmentCB);
        departmentCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // --- Configuración de viajes ---
        Travel allTravels = new Travel();
        allTravels.setId(0);
        allTravels.setDescriptor("Todos");
        allTravels.setBus(0);
        tripCB.getItems().add(allTravels);

        List<Travel> travels = travelS.findAll();

        // Ordenar por fecha
        travels.sort((t1, t2) -> {
            LocalDate d1 = null, d2 = null;
            try {
                if (t1.getDescriptor() != null && !t1.getDescriptor().isEmpty()) {
                    d1 = LocalDate.parse(t1.getDescriptor().split("-")[0], formatter_Show_Date);
                }
                if (t2.getDescriptor() != null && !t2.getDescriptor().isEmpty()) {
                    d2 = LocalDate.parse(t2.getDescriptor().split("-")[0], formatter_Show_Date);
                }
            } catch (Exception e) {
                return 0;
            }

            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return -1;
            }
            if (d2 == null) {
                return 1;
            }
            return d1.compareTo(d2);
        });

        tripCB.getItems().addAll(travels);
        tripCB.setValue(tripCB.getItems().isEmpty() ? null : tripCB.getItems().get(0));

        ChoiceBoxUtil.setTravelConverter(tripCB);
        tripCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // --- Filtro de fecha ---
        sign_upDP.setShowWeekNumbers(false);
        sign_upDP.setConverter(new LocalDateStringConverter(formatter_Show_Date, null));

        // --- Tabla de viajeros ---
        travellerTable.setPlaceholder(new Label("No existen inscripciones"));

        List<Traveller> travellers = travellerS.findAll();
        if (travellers != null) {
            travellerTable.setItems(FXCollections.observableList(travellers));
            amount.setText("Inscripciones en Total: " + travellers.size());
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
                    applyAllFilters();
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
                applyAllFilters();
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
