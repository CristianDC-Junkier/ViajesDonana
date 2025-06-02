package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controlador que controla el dialog de errores, 
 * mostrandole al usuario su error.
 * 
 * @author Cristian
 * @since 2025-05-12
 * @version 1.1
 */
public class ErrorController {

    @FXML
    private Label errorMessageLabel; 

    private Stage dialogStage; 

    private void setErrorMessageLabel(String errorMessageLabel) {
        this.errorMessageLabel.setText(errorMessageLabel);
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage getDialogStage() {
        return dialogStage;
    }
    
    
    @FXML
    private void exitErrorDialog() {
        dialogStage.close();
    }

    /**
     * Muestra el cuadro de diálogo de error de forma modal.
     *
     * @param parentStage El escenario principal (o la ventana que invoca este
     * diálogo)
     * @param errorMessage El mensaje de error que se quiere mostrar
     */
    public static void showErrorDialog(Stage parentStage, String errorMessage) {
        try {
            // Cargar el FXML del cuadro de diálogo de error
            FXMLLoader loader = new FXMLLoader(ErrorController.class.getResource("/ayuntamiento/viajes/view/error.fxml"));
            AnchorPane page = loader.load();

            ErrorController errorController = loader.getController();
            errorController.setErrorMessageLabel(errorMessage);

            errorController.setDialogStage(new Stage());
            errorController.getDialogStage().setTitle("Error");
            errorController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            errorController.getDialogStage().initOwner(parentStage);
            errorController.getDialogStage().getIcons().add(new Image(ErrorController.class.getResourceAsStream("/ayuntamiento/viajes/icons/icon-error.png")));

            // Establecer la escena y mostrar el diálogo
            Scene scene = new Scene(page, 350, 250);
            errorController.getDialogStage().setScene(scene);
            errorController.getDialogStage().showAndWait(); 

        } catch (IOException e) {
           log("Error al cargar el diálogo de error con padre: " + parentStage);
        }
    }

}
