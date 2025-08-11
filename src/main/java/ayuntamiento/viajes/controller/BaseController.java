package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import ayuntamiento.viajes.common.ManagerUtil;
import static ayuntamiento.viajes.common.ManagerUtil.getPage;
import ayuntamiento.viajes.controller.InfoController.DialogResult;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.service.AdminService;
import ayuntamiento.viajes.service.LoginService;

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
 * @since 2025-06-03
 * @version 1.3
 */
public abstract class BaseController {

    @FXML
    protected Node father;
    @FXML
    private Label users;
    protected final String errorStyle
            = "-fx-background-color: "
            + "linear-gradient"
            + "(from 0% 0% to 100% 100%, #e52d27, #b31217);";

    /**
     * Muestra el error ocurrido para el usuario y para el log
     *
     * @param cE La excepción que ocurrió
     */
    public void error(ControledException cE) {
        Stage parentStage = (Stage) father.getScene().getWindow();
        ErrorController.showErrorDialog(parentStage, cE.getMessage());
        log("Error - " + cE.getWhere() + ": \n" + cE.getMessage());
    }

    /**
     * Muestra un dialogo de error de una excepcion warning que solo va al log
     *
     * @param qE La excepción que ocurrió
     */
    public void error(QuietException qE) {
        log("Warning - " + qE.getWhere() + ": \n" + qE.getMessage());
    }

    /**
     * Muestra un dialogo de error de una excepcion no controlada para el
     * usuario un error génerico, para el log, su explicación.
     *
     * @param ex La excepción que ocurrió
     */
    public void error(Exception ex) {
        Stage parentStage = (Stage) father.getScene().getWindow();
        ErrorController.showErrorDialog(parentStage,
                "Ocurrio un error inesperado, "
                + "intentelo de nuevo más tarde, "
                + "o pongase en contacto con los técnicos");
        log("Error No Controlado - " + getPage() + ": \n" + ex.getMessage());
    }

    /**
     * Muestra un dialogo de información o para pedir confirmación
     *
     * @param info El mensaje a mostrar
     * @param needConfirmation true si se pide confirmación
     *
     * @return la confirmación, el rechazo, o indicando que no se necesitaba.
     */
    public DialogResult info(String info, boolean needConfirmation) {
        Stage parentStage = (Stage) father.getScene().getWindow();
        if (needConfirmation == false) {
            log("Info - " + info);
        }
        return InfoController.showInfoDialog(parentStage, info, needConfirmation);
    }

    /**
     * Cambiar el texto del menú de usuarios/perfil segun el usuario
     */
    public void showUserOption() {
        if (LoginService.getAdminLog().getId() == 1) {
            users.setText("Administración de Usuarios");
        } else {
            users.setText("Mi perfil");
        }
    }

    @FXML
    private void userspanel() throws IOException {
        if (LoginService.getAdminDepartment() == 7) {
            try {
                AdminService.rechargeList();
                ManagerUtil.moveTo("admin");
            } catch (ControledException cE) {
                error(cE);
            } catch (QuietException qE) {
                error(qE);
                ManagerUtil.moveTo("admin");
            } catch (Exception ex) {
                error(ex);
            }

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
        LoginService.setAdminLog(null);
        ManagerUtil.moveTo("login");

    }

    @FXML
    private void serviceterms() throws IOException {
        ManagerUtil.moveTo("service_terms");

    }

    @FXML
    public static <T> void refreshTable(final TableView<T> table, final List<T> tableList) {
        table.setItems(null);
        table.layout();
        table.setItems(FXCollections.observableList(tableList));
    }
}
