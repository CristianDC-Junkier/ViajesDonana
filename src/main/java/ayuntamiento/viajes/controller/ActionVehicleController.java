package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.model.Vehicle;
import ayuntamiento.viajes.model.Vehicle.VehicleStatus;
import ayuntamiento.viajes.model.Vehicle.VehicleType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Clase que se encarga de la pestaña de añadir y modificar vehículos, es un
 * dialog modal, que depende de VehicleController
 *
 * @author Ramón Iglesias Granados
 * @since 2025-05-14
 * @version 1.2
 */
public class ActionVehicleController implements Initializable {

    private static Vehicle vSelected;
    private static Vehicle vResult;

    private Stage dialogStage;

    @FXML
    private Label titleLabel;
    @FXML
    private TextField destinationTF;
    @FXML
    private TextField numplateTF;
    @FXML
    private TextField vehicleTF;
    @FXML
    private TextField allocationTF;
    @FXML
    private ChoiceBox<VehicleType> typeCB;
    @FXML
    private ChoiceBox<VehicleStatus> statusCB;
    @FXML
    private DatePicker itvrentDP;
    @FXML
    private DatePicker insuranceDP;
    @FXML
    private TextField km_lastCheckTF;
    @FXML
    private DatePicker lastCheckDP;
    @FXML
    private ImageView imageITVRent;
    @FXML
    private Button actionButton;

    private static int typeAction;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    @FXML
    private void numplateChange() {
        numplateTF.setStyle("");
    }

    @FXML
    private void destinationChange() {
        destinationTF.setStyle("");
    }

    @FXML
    private void vehicleChange() {
        vehicleTF.setStyle("");
    }

    @FXML
    private void allocationChange() {
        allocationTF.setStyle("");
    }

    @FXML
    private void itvrentChange() {
        itvrentDP.setStyle("");
    }

    @FXML
    private void insuranceChange() {
        insuranceDP.setStyle("");
    }

    @FXML
    private void lastCheckChange() {
        lastCheckDP.setStyle("");
    }

    @FXML
    private void extractVehicle() {

        if (!checkFields() || !checkDates()) {
            return;
        }

        vResult = new Vehicle();
        vResult.setDestination(destinationTF.getText().isBlank() ? null : destinationTF.getText());
        vResult.setNumplate(numplateTF.getText());
        vResult.setVehicle(vehicleTF.getText());
        vResult.setAllocation(allocationTF.getText().isBlank() ? null : allocationTF.getText());
        vResult.setType(typeCB.getValue().ordinal());
        vResult.setStatus(statusCB.getValue().ordinal());

        vResult.setItv_RentDate(itvrentDP.getValue());
        vResult.setInsuranceDate(insuranceDP.getValue());
        vResult.setLast_CheckDate(lastCheckDP.getValue());

        vResult.setKms_last_check(km_lastCheckTF.getText().isBlank() ? null : Integer.valueOf(km_lastCheckTF.getText()));

        if (typeAction == 1) {
            vResult.setId(vSelected.getId());
        }

        dialogStage.close();
    }

    private Vehicle getvResult() {
        return vResult;
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage getDialogStage() {
        return dialogStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusCB.getItems().setAll(VehicleStatus.values());
        statusCB.setValue(VehicleStatus.Buen_Estado);
        typeCB.getItems().setAll(VehicleType.values());
        typeCB.setValue(VehicleType.Propiedad);

        typeCB.valueProperty().addListener((obs, oldType, newType) -> changeType());

        /*Cambiar el titulo y los labels dependiendo del tipo*/
        if (typeAction == 0) {
            actionButton.setText("Añadir");
            titleLabel.setText("Añadir Vehiculo");
        } else {
            actionButton.setText("Modificar");
            titleLabel.setText("Modificar Vehiculo");
            populateFields();
        }

        /*Comprobar que solo entran números*/
        km_lastCheckTF.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

    }

    /**
     * Metodo que llama el controlador de vehículos recoge el vehicle
     * seleccionado si fuera modificar el tipo que le llega, siendo 0 = añadir y
     * 1 = modificar y el vista/escena que lo llama
     *
     * @param parent vista/escena que lo llama
     * @param selected vehículo que se modifica, en otro caso, null
     * @param type 0 si es añadir, 1 si es modificar
     *
     * @return vehículo que se añadirá, o que se modificará
     * @throws Exception errores en la carga del dialogo
     */
    public static Vehicle showActionVehicle(Stage parent, Vehicle selected, int type) throws Exception {
        try {
            typeAction = type;
            vResult = null;
            vSelected = selected;

            FXMLLoader loader = new FXMLLoader(ActionVehicleController.class.getResource("/ayuntamiento/viajes/view/actionvehicle.fxml"));
            StackPane page = loader.load();
            ActionVehicleController actionController = loader.getController();

            actionController.setDialogStage(new Stage());
            actionController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            actionController.getDialogStage().initOwner(parent);
            actionController.getDialogStage().getIcons().add(new Image(ErrorController.class.getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));

            if (typeAction == 0) {
                actionController.getDialogStage().setTitle("Vehiculos - Añadir Vehículo");
            } else {
                actionController.getDialogStage().setTitle("Vehiculos - Modificar Vehículo");
            }

            /*Establecer la escena y mostrar el diálogo*/
            Scene scene = new Scene(page, 700, 600);
            actionController.getDialogStage().setScene(scene);
            actionController.getDialogStage().showAndWait();

            return actionController.getvResult();

        } catch (Exception e) {
            throw new Exception("Error al cargar el diálogo para añadir o modificar vehículos");
        }
    }

    /*Si Cambia a alquiler cambiar la imagen del ITV/Alquiler*/
    private void changeType() {
        if (typeCB.getValue() != VehicleType.Alquiler) {
            imageITVRent.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/car-itv.png").toExternalForm()));
        } else {
            imageITVRent.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/car-rent.png").toExternalForm()));
        }
    }

    private void populateFields() {
        destinationTF.setText(vSelected.getDestination());
        numplateTF.setText(vSelected.getNumplate());
        vehicleTF.setText(vSelected.getVehicle());
        allocationTF.setText(vSelected.getAllocation());
        typeCB.setValue(vSelected.getType());
        statusCB.setValue(vSelected.getStatus());
        itvrentDP.setValue(vSelected.getItv_RentDate());
        insuranceDP.setValue(vSelected.getInsuranceDate());
        km_lastCheckTF.setText(vSelected.getKms_last_check() != null ? String.valueOf(vSelected.getKms_last_check()) : "");
        lastCheckDP.setValue(vSelected.getLast_CheckDate());

    }

    private boolean checkFields() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";

        if (SecurityUtil.checkBadString(destinationTF.getText())) {
            destinationTF.setStyle(errorStyle);
            correct = false;
        } else if (SecurityUtil.checkBadOrEmptyString(numplateTF.getText())) {
            numplateTF.setStyle(errorStyle);
            correct = false;
        } else if (SecurityUtil.checkBadOrEmptyString(vehicleTF.getText())) {
            vehicleTF.setStyle(errorStyle);
            correct = false;
        } else if (SecurityUtil.checkBadString(allocationTF.getText())) {
            allocationTF.setStyle(errorStyle);
            correct = false;
        }

        return correct;
    }

    private boolean checkDates() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";

        if (!isValidDateFormat(itvrentDP, formatter_Show_Date, errorStyle)) {
            correct = false;
        }
        if (!isValidDateFormat(insuranceDP, formatter_Show_Date, errorStyle)) {
            correct = false;
        }
        if (!isValidDateFormat(lastCheckDP, formatter_Show_Date, errorStyle)) {
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
