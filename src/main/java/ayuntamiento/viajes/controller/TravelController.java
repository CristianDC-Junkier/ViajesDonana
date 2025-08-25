package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ChoiceBoxUtil;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class TravelController extends BaseController implements Initializable {

    private final static TravelService travelS;
    private final static DepartmentService departmentS;

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
        departmentS = new DepartmentService();
    }

    @FXML
    private void add() {
        showActionDialogTravel(0);
    }
    @FXML
    private void modify() {
        showActionDialogTravel(1);
    }
    @FXML
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
        showUserOption();

        descriptorColumn.setCellValueFactory(new PropertyValueFactory<Travel, String>("descriptor"));
        //Callbacks para mostrar los departamentos por nombre en vez de por el ID
        departmentColumn.setCellValueFactory(new Callback<CellDataFeatures<Travel, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Travel, String> p) {
                return new SimpleStringProperty(departmentS.findById(p.getValue().getDepartment()).get().getName().replace('_', ' '));
            }
        });
        oSeatsColumn.setCellValueFactory(new PropertyValueFactory<Travel, Integer>("seats_occupied"));
        tSeatsColumn.setCellValueFactory(new PropertyValueFactory<Travel, Integer>("seats_total"));

        travelTable.setItems(FXCollections.observableList(travelS.findAll()));
        travelTable.setPlaceholder(new Label("No existen viajes"));

        Department allDepartment = new Department();
        allDepartment.setId(0);
        allDepartment.setName("Todos");
        departmentCB.getItems().add(allDepartment);

        // Carga departamentos desde DepartmentService
        List<Department> departments = departmentS.findAll();
        departmentCB.getItems().addAll(departments);
        departmentCB.setValue(departmentCB.getItems().get(0));
        ChoiceBoxUtil.setDepartmentNameConverter(departmentCB);
        departmentCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());
        
        amount.setText("Viajes en Total: " + travelS.findAll().size());
    }
    
    public void showActionDialogTravel(int mode) {
        if (mode == 1 && travelTable.getSelectionModel().getSelectedItem() == null) {
            error(new ControledException("Debe seleccionar un viaje de la tabla",
                    "TravelController - showActionDialogTravel"));
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
        long selectedDepartment = departmentCB.getValue().getId();

        List<Travel> filtered = travelS.findAll().stream()
                .filter(t
                        -> (nameText.isEmpty() || (t.getDescriptor() != null && t.getDescriptor().toLowerCase().contains(nameText)))
                && (selectedDepartment == 0 || (t.getDepartment() == selectedDepartment))
                )
                .collect(Collectors.toList());

        refreshTable(travelTable, filtered);
    }

    @FXML
    private void resetAllFilters() {
        descriptorTF.setText("");
        departmentCB.setValue(departmentCB.getItems().get(0));

        applyAllFilters();
    }
}
