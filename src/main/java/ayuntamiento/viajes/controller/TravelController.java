package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.controller.BaseController.refreshTable;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.TravelService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class TravelController extends BaseController implements Initializable {

    private final static TravelService travelS;
    private final static DepartmentService DepartmentS;

    @FXML
    private TableView<Travel> travelTable;
    @FXML
    private TableColumn descriptorColumn;
    @FXML
    private TableColumn departmentColumn;
    @FXML
    private TableColumn oSeatsColumn;
    @FXML
    private TableColumn tSeatsColumn;

    @FXML
    private Label amount;

    @FXML
    private TextField descriptorTF;
    @FXML
    private ChoiceBox<Department> departmentCB;

    static {
        travelS = new TravelService();
        DepartmentS = new DepartmentService();
    }

    private void add() {
        showActionDialog(0);
    }

    private void modify() {
        showActionDialog(1);
    }

    private void delete() {
        if (travelTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un viaje de la tabla",
                    "TravelController - delete"));
        } else {
            try {
                if (info("¿Está seguro de que quiere eliminar este viaje?", true) == InfoController.DialogResult.ACCEPT) {
                    if (travelS.delete(travelTable.getSelectionModel().getSelectedItem())) {
                        info("El viaje fue eliminado con éxito", false);
                        refreshTable(travelTable, travelS.findAll());
                    } else {
                        throw new Exception("El viaje no pudo ser borrado");
                    }
                }
            } catch (Exception ex) {
                refreshTable(travelTable, travelS.findAll());
                error(ex);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        descriptorColumn.setCellValueFactory(new PropertyValueFactory<Travel, String>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<Travel, Department>("department"));
        oSeatsColumn.setCellValueFactory(new PropertyValueFactory<Travel, Integer>("seats_occupied"));
        tSeatsColumn.setCellValueFactory(new PropertyValueFactory<Travel, Integer>("seats_total"));

        travelTable.setPlaceholder(new Label("No existen viajes"));
        
        /*departmentCB.getItems().add("Todos");
        for (Departments type : Departments.values()) {
            departmentCB.getItems().add(type.toString());
        }*/
        departmentCB.getSelectionModel().selectFirst();

        // Carga departamentos desde DepartmentService
        List<Department> departments = DepartmentS.findAll();
        departmentCB.getItems().setAll(departments);
        departmentCB.setValue(departmentCB.getItems().get(0));

        // Para mostrar solo el nombre en departmnetCB
        departmentCB.setConverter(new StringConverter<Department>() {
            @Override
            public String toString(Department department) {
                return department == null ? "" : department.getName();
            }

            @Override
            public Department fromString(String string) {
                return null;
            }
        });
        
        /*departmentCB.getItems().add("Todos");
        for (Departments type : Departments.values()) {
            departmentCB.getItems().add(type.toString());
        }*/
        departmentCB.getSelectionModel().selectFirst();
        departmentCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());
        amount.setText("Viajes en Total: " + travelS.findAll().size());
    }
    
    public void showActionDialog(int mode) {
        if (mode == 1 && travelTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un viaje de la tabla",
                    "TravelController - showActionDialog"));
        } else {
            try {
                Travel vResult = ActionTravelController.showActionTravel((Stage) father.getScene().getWindow(),
                        travelTable.getSelectionModel().getSelectedItem(), mode);
                if (vResult != null) {
                    if (mode == 0) {
                        anadir(vResult);
                        info("El Viaje fue añadido correctamente", false);
                    } else {
                        modificar(vResult);
                        info("El Viaje fue modificado correctamente", false);
                    }
                    refreshTable(travelTable, travelS.findAll());
                }
            } catch (ControledException cE) {
                refreshTable(travelTable, travelS.findAll());
                error(cE);
            } catch (Exception ex) {
                error(ex);
            }
        }
    }
    
    public void anadir(Travel entity) throws Exception {
        if (travelS.save(entity) == null) {
            refreshTable(travelTable, travelS.findAll());
            throw new ControledException("La descripción introducida ya existe: " + entity.getDescriptor(),
                    "TravellController - anadir");
        }
        refreshTable(travelTable, travelS.findAll());
    }

    public void modificar(Travel entity) throws Exception {
        if (travelS.modify(entity) == null) {
            refreshTable(travelTable, travelS.findAll());
            throw new ControledException("La descripción introducida ya existe: " + entity.getDescriptor(),
                    "TravelController - modficar");
        }
        refreshTable(travelTable, travelS.findAll());
    }

    @FXML
    private void applyAllFilters() {
        String nameText = descriptorTF.getText() != null ? descriptorTF.getText().toLowerCase().trim() : "";
        String selectedDepartment = departmentCB.getValue().toString();

        /*List<Travel> filtered = travelS.findAll().stream()
                .filter(t
                        -> (nameText.isEmpty() || (t.getDescriptor() != null && t.getDescriptor().toLowerCase().contains(nameText)))
                && (selectedDepartment.equals("Todos") || (t.getDepartment() != null && t.getDepartment().toString().equalsIgnoreCase(selectedDepartment)))
                )
                .collect(Collectors.toList());

        refreshTable(travelTable, filtered);*/
    }

    @FXML
    private void resetAllFilters() {
        descriptorTF.setText("");
        departmentCB.setValue(departmentCB.getItems().get(0));

        applyAllFilters();
    }
}
