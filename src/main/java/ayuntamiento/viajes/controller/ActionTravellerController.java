package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.model.Traveller.TravellerTrip;
import ayuntamiento.viajes.model.Traveller.TravellerOffice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

/**
 * Clase que se encarga de la pestaña de añadir y modificar vehículos, es un
 * dialog modal, que depende de TravellerController
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-02
 * @version 1.2
 */
public class ActionTravellerController implements Initializable {

    private static Traveller tSelected;
    private static Traveller tResult;

    private Stage dialogStage;

    @FXML
    private Label titleLabel;
    @FXML
    private TextField dniTF;
    @FXML
    private TextField nameTF;
    @FXML
    private ChoiceBox<TravellerOffice> officeCB;
    @FXML
    private ChoiceBox<TravellerTrip> tripCB;
    @FXML
    private DatePicker sign_upDP;
    @FXML
    private Button actionButton;

    private static int typeAction;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    @FXML
    private void dniChange() {
        dniTF.setStyle("");
    }

    @FXML
    private void nameChange() {
        nameTF.setStyle("");
    }


    @FXML
    private void signupChange() {
        sign_upDP.setStyle("");
    }

    @FXML
    private void extract() {

        if (!checkFields() || !checkDates()) {
            return;
        }

        tResult = new Traveller();
        tResult.setDni(dniTF.getText());
        tResult.setName(nameTF.getText());
        tResult.setOffice(officeCB.getValue().ordinal());
        tResult.setTrip(tripCB.getValue().ordinal());

        tResult.setSignUpDate(sign_upDP.getValue());

        if (typeAction == 1) {
            tResult.setId(tSelected.getId());
        }

        dialogStage.close();
    }

    private Traveller gettResult() {
        return tResult;
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage getDialogStage() {
        return dialogStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tripCB.getItems().setAll(TravellerTrip.values());
        tripCB.setValue(tripCB.getItems().get(0));
        officeCB.getItems().setAll(TravellerOffice.values());
        officeCB.setValue(officeCB.getItems().get(0));

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
    public static Traveller showActionTraveller(Stage parent, Traveller selected, int type) throws Exception {
        try {
            typeAction = type;
            tResult = null;
            tSelected = selected;

            FXMLLoader loader = new FXMLLoader(ActionTravellerController.class.getResource("/ayuntamiento/viajes/view/actiontraveller.fxml"));
            StackPane page = loader.load();
            ActionTravellerController actionController = loader.getController();

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
        dniTF.setText(tSelected.getDni());
        nameTF.setText(tSelected.getName());
        officeCB.setValue(tSelected.getOffice());
        tripCB.setValue(tSelected.getTrip());
        sign_upDP.setValue(tSelected.getSignUpDate());

    }

    private boolean checkFields() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";

       if (SecurityUtil.checkBadOrEmptyString(dniTF.getText())) {
            dniTF.setStyle(errorStyle);
            correct = false;
        } else if (SecurityUtil.checkBadOrEmptyString(nameTF.getText())) {
            nameTF.setStyle(errorStyle);
            correct = false;
        }

        return correct;
    }

    private boolean checkDates() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";
        
        if (!isValidDateFormat(sign_upDP, formatter_Show_Date, errorStyle)) {
            correct = false;
        }

        return correct;
    }

    private boolean isValidDateFormat(DatePicker datePicker, DateTimeFormatter formatter, String errorStyle) {
        try {
            if (datePicker.getValue() != null) {
                formatter.format(datePicker.getValue());
            }
            return true;
        } catch (DateTimeException e) {
            datePicker.setStyle(errorStyle);
            return false;
        }
    }

}
