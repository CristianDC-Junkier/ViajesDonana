package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.TravelService;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class ActionTravelController implements Initializable {

    private final static TravelService TravelS;
    private final static DepartmentService DepartmentS;

    private static Travel tSelected;
    private static Travel tResult;

    private Stage dialogStage;

    @FXML
    private Label titleLabel;
    @FXML
    private TextField descriptorTF;
    @FXML
    private TextField tSeatsTF;
    @FXML
    private ChoiceBox<Department> departmentCB;
    @FXML
    private Button actionButton;

    private static int typeAction;
    
    static {
        TravelS = new TravelService();
        DepartmentS = new DepartmentService();
    }
    
    @FXML
    private void descriptorChange() {
        descriptorTF.setStyle("");
    }

    @FXML
    private void oSeatsChange() {
        tSeatsTF.setStyle("");
    }
    
    @FXML
    private void extract() {

        if (!checkFields()) {
            return;
        }

        tResult = new Travel();
        tResult.setDescriptor(descriptorTF.getText());
        tResult.setSeats_total(Integer.parseInt(tSeatsTF.getText()));
        tResult.setDepartment(departmentCB.getValue());
        tResult.setSeats_ocuppied(0);

        if (typeAction == 1) {
            tResult.setId(tSelected.getId());
        }

        dialogStage.close();
    }
    
    private Travel gettResult() {
        return tResult;
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage getDialogStage() {
        return dialogStage;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tSeatsTF.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        
        List<Department> departments = DepartmentS.findAll();
        departmentCB.getItems().setAll(departments);
        departmentCB.setValue(departmentCB.getItems().get(0));
        
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
        
        /*Cambiar el titulo y los labels dependiendo del tipo*/
        if (typeAction == 0) {
            actionButton.setText("Añadir");
            titleLabel.setText("Añadir Viajero");
        } else {
            actionButton.setText("Modificar");
            titleLabel.setText("Modificar Viajero");
            populateFields();
        }
    }
    
    /**
     * Metodo que llama el controlador de Traveller recoge el traveller
     * seleccionado si fuera modificar el tipo que le llega, siendo 0 = añadir y
     * 1 = modificar y el vista/escena que lo llama
     *
     * @param parent vista/escena que lo llama
     * @param selected traveller que se modifica, en otro caso, null
     * @param type 0 si es añadir, 1 si es modificar
     *
     * @return traveller que se añadirá, o que se modificará
     * @throws Exception errores en la carga del dialogo
     */
    public static Travel showActionTravel(Stage parent, Travel selected, int type) throws Exception {
        try {
            typeAction = type;
            tResult = null;
            tSelected = selected;

            FXMLLoader loader = new FXMLLoader(ActionTravellerController.class.getResource("/ayuntamiento/viajes/view/actiontraveller.fxml"));
            StackPane page = loader.load();
            ActionTravelController actionController = loader.getController();

            actionController.setDialogStage(new Stage());
            actionController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            actionController.getDialogStage().initOwner(parent);
            actionController.getDialogStage().getIcons().add(new Image(ErrorController.class.getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));

            if (typeAction == 0) {
                actionController.getDialogStage().setTitle("Viajes Doñana - Añadir Viajante");
            } else {
                actionController.getDialogStage().setTitle("Viajes Doñana - Modificar Viajante");
            }

            /*Establecer la escena y mostrar el diálogo*/
            Scene scene = new Scene(page, 700, 600);
            actionController.getDialogStage().setScene(scene);
            actionController.getDialogStage().showAndWait();

            return actionController.gettResult();

        } catch (Exception e) {
            throw new Exception("Error al cargar el diálogo para añadir o modificar viajantes");
        }
    }
    
    private void populateFields() {
        descriptorTF.setText(tSelected.getDescriptor());
        tSeatsTF.setText(String.valueOf(tSelected.getSeats_total()));
        departmentCB.setValue(tSelected.getDepartment());
    }
    
    private boolean checkFields() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";

        if (SecurityUtil.checkBadOrEmptyString(descriptorTF.getText())
                || !SecurityUtil.checkDNI_NIE(descriptorTF.getText())) {
            descriptorTF.setStyle(errorStyle);
            correct = false;
        } else if (SecurityUtil.checkBadOrEmptyString(tSeatsTF.getText())) {
            tSeatsTF.setStyle(errorStyle);
            correct = false;
        }

        return correct;
    }
    
}
