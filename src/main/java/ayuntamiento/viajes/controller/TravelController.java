package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.controller.BaseController.refreshTable;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
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

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class TravelController extends BaseController implements Initializable {

    private final TravelService travelS;
    
    @FXML
    private TableView<Travel> travelTable;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn departmentColumn;
    @FXML 
    private TableColumn oSeatsColumn;
    @FXML
    private TableColumn tSeatsColumn;
    
    @FXML
    private Label amount;
    
    @FXML
    private TextField nameTF;
    @FXML
    private ChoiceBox departmentCB;
    
    public TravelController() {
        travelS = new TravelService();
    }
    
    private void add(){
        //TO DO
    }
    private void modify(){
        //TO DO
    }
    private void delete(){
        //TO DO
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        nameColumn.setCellValueFactory(new PropertyValueFactory<Travel, String>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<Travel, Department>("department"));
        oSeatsColumn.setCellValueFactory(new PropertyValueFactory<Travel, Integer>("seats_occupied"));
        tSeatsColumn.setCellValueFactory(new PropertyValueFactory<Travel, Integer>("seats_total"));
        
        travelTable.setPlaceholder(new Label("No existen viajes"));
        
        /*departmentCB.getItems().add("Todos");
        for (Departments type : Departments.values()) {
            departmentCB.getItems().add(type.toString());
        }*/
        departmentCB.getSelectionModel().selectFirst();
        departmentCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());
    }    
    
    @FXML
    private void applyAllFilters() {
        String nameText = nameTF.getText() != null ? nameTF.getText().toLowerCase().trim() : "";
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
        nameTF.setText("");
        departmentCB.setValue(departmentCB.getItems().get(0));

        applyAllFilters();

    }
    
}
