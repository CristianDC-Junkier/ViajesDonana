package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.LoggerUtil;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controlador que controla el dialog de información, 
 * mostrandole al usuario la información o pidiendole
 * confirmación sobre algo.
 * 
 * @author Cristian Delgado Cruz
 * @since 2025-06-05
 * @version 1.0
 */
public class InfoController {

    @FXML
    private Label infoMessageLabel;
    @FXML
    private Button infoAcceptButton;
    @FXML
    private Button infoRejectButton;
    @FXML
    private Button infoExitButton;

    private Stage dialogStage;
    private static DialogResult result;

    public enum DialogResult {
        NOMATTER,
        ACCEPT,
        REJECT
    }

    @FXML
    private void acceptInfoDialog() {
        result = DialogResult.ACCEPT;
        dialogStage.close();
    }

    @FXML
    private void rejectInfoDialog() {
        result = DialogResult.REJECT;
        dialogStage.close();
    }
    
    @FXML
    private void exitInfoDialog() {
        result = DialogResult.NOMATTER;
        dialogStage.close();
    }

    private void setInfoMessageLabel(String infoMessageLabel) {
        this.infoMessageLabel.setText(infoMessageLabel);
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    /**
    * Si la visibilidad es true, se considera un metodo de pedir
    * confirmación, si no, se considera que solo da información, 
    * colocando el result en NOMATTER.
    * 
    * @param visibilityButtons pide información o no
    */
    private void setVisibility(boolean visibilityButtons) {
        if(visibilityButtons == true){
            result = DialogResult.REJECT;
        }else{
            result = DialogResult.NOMATTER;
        }
        infoAcceptButton.setVisible(visibilityButtons);
        infoRejectButton.setVisible(visibilityButtons);
        infoExitButton.setVisible(!visibilityButtons);
        infoAcceptButton.setFocusTraversable(visibilityButtons);
        infoRejectButton.setFocusTraversable(visibilityButtons);
        infoExitButton.setFocusTraversable(!visibilityButtons);
    }

    private Stage getDialogStage() {
        return dialogStage;
    }

    private DialogResult getResult() {
        return result;
    }

    /**
     * Muestra el cuadro de diálogo de información de forma modal.
     *
     * @param parentStage El escenario principal (o la ventana que invoca este
     * diálogo)
     * @param infoMessage El mensaje de información que se quiere mostrar
     * @param needConfirmation true para pedir confirmación, false para mostrar 
     * información
     * @return Booleano de 3 estados para controlar que se decidió
     */
    public static DialogResult showInfoDialog(Stage parentStage, String infoMessage, boolean needConfirmation){
        try {
            // Cargar el FXML del cuadro de diálogo de error
            FXMLLoader loader = new FXMLLoader(InfoController.class.getResource("/ayuntamiento/viajes/view/info.fxml"));
            AnchorPane page = loader.load();

            InfoController infoController = loader.getController();
            infoController.setVisibility(needConfirmation);
            infoController.setInfoMessageLabel(infoMessage);

            infoController.setDialogStage(new Stage());
            infoController.getDialogStage().setTitle("Información");
            infoController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            infoController.getDialogStage().initOwner(parentStage);
            infoController.getDialogStage().getIcons().add(new Image(InfoController.class.getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));

            // Establecer la escena y mostrar el diálogo 
            Scene scene = new Scene(page, 350, 250);
            infoController.getDialogStage().setScene(scene);
            infoController.getDialogStage().showAndWait();

            return infoController.getResult();

        } catch (IOException e) {
            LoggerUtil.log("Error al cargar el diálogo de información con padre: " + parentStage);
        }
        return DialogResult.REJECT;
    }

}
