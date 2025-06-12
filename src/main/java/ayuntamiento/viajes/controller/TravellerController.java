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

import ayuntamiento.viajes.service.TravellerService;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.model.Traveller.TravellerTrip;
import ayuntamiento.viajes.model.Traveller.TravellerOffice;
import java.io.IOException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.converter.LocalDateStringConverter;

/**
 * Clase Controladora que se encarga de gestionar los travellers existentes
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-03
 * @version 1.2
 */
public class TravellerController extends BaseController implements Initializable {

    private final TravellerService travellerS;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    public TravellerController() throws IOException, InterruptedException {
        travellerS = new TravellerService();
        try {
            travellerS.rechargeList();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    @FXML
    private TableView<Traveller> travellerTable;
    @FXML
    private TableColumn dniColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn officeColumn;
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
    private ChoiceBox tripCB;
    @FXML
    private ChoiceBox officeCB;
    @FXML
    private DatePicker sign_upDP;

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
        officeColumn.setCellValueFactory(new PropertyValueFactory<Traveller, TravellerOffice>("office"));
        tripColumn.setCellValueFactory(new PropertyValueFactory<Traveller, TravellerTrip>("trip"));
        signupColumn.setCellValueFactory(new PropertyValueFactory<Traveller, LocalDate>("signup"));

        travellerTable.setPlaceholder(new Label("No existen inscripciones"));

        // Filtro de titularidad
        officeCB.getItems().add("Todos");
        for (TravellerOffice type : TravellerOffice.values()) {
            officeCB.getItems().add(type.toString());
        }
        officeCB.getSelectionModel().selectFirst();
        officeCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // Filtro de estado
        tripCB.getItems().add("Todos");
        for (TravellerTrip status : TravellerTrip.values()) {
            tripCB.getItems().add(status.toString());
        }
        tripCB.getSelectionModel().selectFirst();
        tripCB.valueProperty().addListener((obs, oldType, newType) -> applyAllFilters());

        // Filtro de DatePicker
        sign_upDP.setShowWeekNumbers(false);
        sign_upDP.setConverter(new LocalDateStringConverter(formatter_Show_Date, null));

        travellerTable.setItems(FXCollections.observableList(travellerS.findAll()));

        travellerTable.setPlaceholder(new Label("No existen inscripciones"));
        amount.setText("Inscripciones en Total: " + travellerS.findAll().size());

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
            } catch (ControledException ex) {
                error(ex);
            } catch (Exception ex) {
                error(ex);
            }
        }
    }

    public void anadir(Traveller entity) throws Exception {
        if (travellerS.save(entity) == null) {
            throw new ControledException("El DNI introducido ya existe: " + entity.getDni(),
                    "TravellerController - anadir");
        }
        refreshTable(travellerTable, travellerS.findAll());
    }

    public void modificar(Traveller entity) throws Exception {
        if (travellerS.modify(entity) == null) {
            throw new ControledException("El DNI introducido ya existe: " + entity.getDni(),
                    "TravellerController - anadir");
        }
        refreshTable(travellerTable, travellerS.findAll());
    }

    @FXML
    private void applyAllFilters() {
        String nameText = nameTF.getText() != null ? nameTF.getText().toLowerCase().trim() : "";
        String dniText = dniTF.getText() != null ? dniTF.getText().toLowerCase().trim() : "";

        String selectedOffice = officeCB.getValue().toString();
        String selectedTrip = tripCB.getValue().toString();

        LocalDate selectedInsuranceDate = sign_upDP.getValue();

        List<Traveller> filtered = travellerS.findAll().stream()
                .filter(t
                        -> (nameText.isEmpty() || (t.getName() != null && t.getName().toLowerCase().contains(nameText)))
                && (dniText.isEmpty() || (t.getDni() != null && t.getDni().toLowerCase().contains(dniText)))
                && (selectedOffice.equals("Todos") || (t.getOffice() != null && t.getOffice().toString().equalsIgnoreCase(selectedOffice)))
                && (selectedTrip.equals("Todos") || (t.getTrip() != null && t.getTrip().toString().equalsIgnoreCase(selectedTrip)))
                && (selectedInsuranceDate == null || (t.getSignUpDate() != null && t.getSignUpDate().isBefore(selectedInsuranceDate)))
                )
                .collect(Collectors.toList());

        refreshTable(travellerTable, filtered);
    }

    @FXML
    private void resetAllFilters() {
        nameTF.setText("");
        dniTF.setText("");

        officeCB.setValue(officeCB.getItems().get(0));
        tripCB.setValue(tripCB.getItems().get(0));

        sign_upDP.setValue(null);

        applyAllFilters();

    }

}
