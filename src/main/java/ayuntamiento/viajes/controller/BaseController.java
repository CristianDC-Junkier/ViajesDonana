package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.controller.InfoController.DialogResult;
import ayuntamiento.viajes.service.UserService;

import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Clase Base que se encarga del control de algunas funciones que están en
 * prácticamente todos los controladores
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-09
 * @version 1.2
 */
public abstract class BaseController {

    @FXML
    protected Node father;
    @FXML
    private Label users;
    
    

    /**
     * Muestra un dialogo de error modal
     *
     * @param error El mensaje de error a mostrar
     */
    public void error(String error) {
        Stage parentStage = (Stage) father.getScene().getWindow();
        ErrorController.showErrorDialog(parentStage, error);
    }

    /**
     * Muestra un dialogo de información
     * o para pedir confirmación
     *
     * @param info El mensaje a mostrar
     * @param needConfirmation true si se pide confirmación
     * 
     * @return la confirmación, el rechazo, o indicando que no se necesitaba.
     */
    public DialogResult info(String info, boolean needConfirmation) {
        Stage parentStage = (Stage) father.getScene().getWindow();
        return InfoController.showInfoDialog(parentStage, info, needConfirmation);
    }

    
    /**
     * Cambiar el texto del menú de usuarios/perfil segun
     * el usuario
     */
    public void showUserOption() {
        if (UserService.getUsuarioLog().getType().ordinal() == 1) {
            users.setText("Administración de Usuarios");
        } else {
            users.setText("Mi perfil");
        }
    }

    @FXML
    private void userspanel() throws IOException {
        if (UserService.getUsuarioLog().getType().ordinal() == 1) {
            ManagerUtil.moveTo("user");
            
        } else {
            ManagerUtil.moveTo("profile");
            
        }
    }

    @FXML
    private void backpanel() throws IOException {
        ManagerUtil.goBack();
    }

    @FXML
    private void exit() throws IOException {
        UserService.setUsuarioLog(null);
        ManagerUtil.moveTo("login");
        
    }
    
    @FXML 
    private void serviceterms() throws IOException{
        ManagerUtil.moveTo("service_terms");
        
    }

    @FXML
    public static <T> void refreshTable(final TableView<T> table, final List<T> tableList) {
        table.setItems(null);
        table.layout();
        table.setItems(FXCollections.observableList(tableList));
    }
}
