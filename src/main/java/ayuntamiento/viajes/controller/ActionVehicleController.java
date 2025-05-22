
package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.model.Vehicle;
import ayuntamiento.viajes.model.Vehicle.VehicleStatus;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Ramón Iglesias
 */
public class ActionVehicleController implements Initializable {

    public static Vehicle result;
    public static Vehicle getResult(){
        return result;
    }
    
    private Stage actionStage;
    private void setDialogStage(Stage actionStage) {
        this.actionStage = actionStage;
    }

    private Stage getDialogStage() {
        return actionStage;
    }
    
    public static String mode;
    public static void setMode(String mode){
        ActionVehicleController.mode = mode;
    }
    public static String getMode(){
        return mode;
    }
    
    public static Vehicle selected;
    public static void setSelected(Vehicle selected){
        ActionVehicleController.selected = selected;
    }
    
    @FXML private TextField numplateTF;
    @FXML private TextField brandTF;
    @FXML private TextField modelTF;
    @FXML private TextField itvTF;
    @FXML private TextField insuranceTF;
    @FXML private Button actionButton;
    @FXML private ComboBox<VehicleStatus> typeCB;
    
    private void exit() {
        actionStage.close();
    }
    
    public static void showActionVehicle(Stage parent) {
        try {
            // Cargar el FXML de la accion correspondiente
            FXMLLoader loader = new FXMLLoader(ErrorController.class.getResource("/ayuntamiento/vehiculos/view/actionvehicle.fxml"));
            AnchorPane page = loader.load();

            ActionVehicleController avController = loader.getController();
            avController.setDialogStage(new Stage());
            if(mode == "Add") avController.getDialogStage().setTitle("Añadir Vehiculo");
            else if(mode == "Mod") avController.getDialogStage().setTitle("Modificar Vehiculo");
            avController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            avController.getDialogStage().initOwner(parent);
            

            // Establecer la escena y mostrar el diálogo
            Scene scene = new Scene(page);
            avController.getDialogStage().setScene(scene);
            avController.getDialogStage().showAndWait(); 

        } catch (IOException e) {
            LoggerUtil.log("Error al cargar el diálogo de acción");
        }
    }
    
    public void extractVehicle(){
        numplateTF.setStyle("");
        brandTF.setStyle("");
        modelTF.setStyle("");
        typeCB.setStyle("");
        itvTF.setStyle("");
        insuranceTF.setStyle("");
        
        if(numplateTF.getText().isBlank()){
            numplateTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(brandTF.getText().isBlank()){
            brandTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(modelTF.getText().isBlank()){
            modelTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(typeCB.getValue() == null){
            typeCB.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(itvTF.getText().isBlank()){
            itvTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(insuranceTF.getText().isBlank()){
            insuranceTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else{
            result = new Vehicle();
            result.setNumplate(numplateTF.getText());
            result.setBrand(brandTF.getText());
            result.setModel(modelTF.getText());
            result.setType(typeCB.getValue().ordinal());
            result.setItv_rent(itvTF.getText());
            result.setInsurance(insuranceTF.getText());
            if(mode == "Mod") result.setId(selected.getId());

            exit();
        }        
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        typeCB.getItems().setAll(VehicleStatus.values());
        
        if (mode == "Add"){
            actionButton.setText("Añadir");
        }
        else if (mode == "Mod"){
            actionButton.setText("Modificar");
            
            numplateTF.setText(selected.getNumplate());
            brandTF.setText(selected.getBrand());
            modelTF.setText(selected.getModel());
            typeCB.setValue(selected.getType());
            itvTF.setText(selected.getItv_rent());
            insuranceTF.setText(selected.getInsurance());
            
        }
    }    
    
}
