package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ChoiceBoxUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.TravelService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

/**
 * Clase que se encarga de la pestaña de añadir y modificar viajes, es un
 * dialog modal, que depende de TravelController
 *
 * @author Ramón Iglesias Granados
 * @since 2025-08-18
 * @version 1.1
 */
public class ActionTravelController implements Initializable {

    private final static TravelService travelS;
    private final static DepartmentService departmentS;

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
        travelS = new TravelService();
        departmentS = new DepartmentService();
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
        tResult.setDepartment(departmentCB.getValue().getId());
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
        
        List<Department> departments = departmentS.findAll();
        departmentCB.getItems().setAll(departments);
        departmentCB.setValue(departmentCB.getItems().get(0));
        
        ChoiceBoxUtil.setDepartmentNameConverter(departmentCB);
        
        /*Cambiar el titulo y los labels dependiendo del tipo*/
        if (typeAction == 0) {
            actionButton.setText("Añadir");
            titleLabel.setText("Añadir Viaje");
        } else {
            actionButton.setText("Modificar");
            titleLabel.setText("Modificar Viaje");
            populateFields();
        }
    }
    
    /**
     * Metodo que llama el controlador de Travel recoge el viaje
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
            
            FXMLLoader loader = new FXMLLoader(ActionTravelController.class.getResource("/ayuntamiento/viajes/view/actiontravel.fxml"));
            StackPane page = loader.load();
            ActionTravelController actionController = loader.getController();
            
            actionController.setDialogStage(new Stage());
            actionController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            actionController.getDialogStage().initOwner(parent);
            actionController.getDialogStage().getIcons().add(new Image(ErrorController.class.getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));
            
            if (typeAction == 0) {
                actionController.getDialogStage().setTitle("Viajes Doñana - Añadir Viaje");
            } else {
                actionController.getDialogStage().setTitle("Viajes Doñana - Modificar Viaje");
            }

            /*Establecer la escena y mostrar el diálogo*/
            Scene scene = new Scene(page, 650, 500);
            actionController.getDialogStage().setScene(scene);
            actionController.getDialogStage().showAndWait();

            return actionController.gettResult();

        } catch (Exception e) {
            throw new Exception("Error al cargar el diálogo para añadir o modificar viajes");
        }
    }
    
    private void populateFields() {
        descriptorTF.setText(tSelected.getDescriptor());
        tSeatsTF.setText(String.valueOf(tSelected.getSeats_total()));
        departmentCB.setValue(departmentS.findById(tSelected.getDepartment()).get());
    }
    
    private boolean checkFields() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";

        if (SecurityUtil.checkBadOrEmptyString(tSeatsTF.getText())) {
            tSeatsTF.setStyle(errorStyle);
            correct = false;
        }

        return correct;
    }
    
}
