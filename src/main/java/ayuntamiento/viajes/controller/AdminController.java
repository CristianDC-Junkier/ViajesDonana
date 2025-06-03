package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.exception.ControledException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;

import ayuntamiento.viajes.service.AdminService;
import ayuntamiento.viajes.model.Admin;

/**
 * Clase controladora que se encarga del funcionamiento de la pestaña de
 * administración de usuarios.
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-03
 * @version 1.6
 */
public class AdminController extends BaseController implements Initializable {

    private final AdminService adminS;

    @FXML
    private TableView<Admin> userTable;
    @FXML
    private TableColumn idColumn;
    @FXML
    private TableColumn userColumn;

    @FXML
    private TextField addUserNameTF;
    @FXML
    private TextField addUserPassTF;

    @FXML
    private TextField modUserNameTF;
    @FXML
    private TextField modUserPassTF;
    @FXML
    private Button modButton;

    @FXML
    private TextField delUserNameTF;
    @FXML
    private Button delButton;

    private final int numMaxChars = 16;

    public AdminController() {
        adminS = new AdminService();
    }

    /**
     * Metodo que añade un usuario Controla que no se envie un nombre o clave de
     * un tamaño superior a 16 carácteres, ni tampoco vacio, colocando el field
     * en rojo si algo falla
     */
    @FXML
    private void add() {
        try {
            if (SecurityUtil.checkBadOrEmptyString(addUserNameTF.getText())) {
                addUserNameTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "UserController - add");

            } else if (SecurityUtil.checkBadOrEmptyString(addUserPassTF.getText())) {
                addUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contaseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "UserController - add");
            } else if (addUserNameTF.getText().length() > numMaxChars) {
                addUserPassTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe contener más de 16 carácteres",
                        "UserController - add");
            } else if (addUserPassTF.getText().length() > numMaxChars) {
                addUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe contener más de 16 carácteres",
                        "UserController - add");
            } else {
                Admin u = new Admin(addUserNameTF.getText(), addUserPassTF.getText());

                if (adminS.save(u) == null) {
                    addUserPassTF.setStyle(errorStyle);
                    throw new ControledException("El nombre de usuario ya existe: " + u.getUsername(),
                            "UserController - add");
                } else {
                    LoggerUtil.log("Usuario: " + u.getUsername() + " añadido correctamente");
                    refreshTable(userTable, adminS.findAll());
                    reset();
                }
            }
        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
            reset();
        }
    }

    /**
     * Metodo que modifica un usuario Controla que no se envie un nombre o clave
     * de un tamaño superior a 16 carácteres, ni tampoco vacio, colocando el
     * field en rojo si algo falla
     */
    @FXML
    private void modify() {
        try {
            if (userTable.getSelectionModel().getSelectedItem() == null) {
                throw new ControledException("Debe seleccionar un usuario de la tabla",
                        "UserController - modify");
            } else if (SecurityUtil.checkBadOrEmptyString(modUserNameTF.getText())) {
                modUserNameTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "UserController - modify");
            } else if (SecurityUtil.checkBadOrEmptyString(modUserPassTF.getText())) {
                modUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "UserController - modify");

            } else if (modUserNameTF.getText().length() > numMaxChars) {
                modUserNameTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe contener más de 16 carácteres ",
                        "UserController - modify");

            } else if (modUserPassTF.getText().length() > numMaxChars) {
                modUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe contener más de 16 carácteres ",
                        "UserController - modify");
            } else {
                Admin u = new Admin(modUserNameTF.getText(), modUserPassTF.getText());
                u.setId(userTable.getSelectionModel().getSelectedItem().getId());

                Admin userMod = adminS.modify(u);
                if (userMod == null) {
                    modUserNameTF.setStyle(errorStyle);
                    throw new ControledException("El nombre de usuario ya existe: " + u.getUsername(), "UserController - modify");
                } else {
                    if (AdminService.getAdminLog().getId() == userMod.getId()) {
                        ManagerUtil.reload();
                    } else {
                        info("Usuario, " + userMod.getUsername() + ", modificado con éxito", false);
                        refreshTable(userTable, adminS.findAll());
                        reset();
                    }
                }
            }
        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
            reset();
        }
    }

    /**
     * Metodo que elimina un usuario, controla que el usuario exista preguntando
     * si de verdad quieres hacerlo, colocando el field en rojo si algo falla
     */
    @FXML
    private void delete() {
        try {
            if (userTable.getSelectionModel().getSelectedItem() == null) {
                delUserNameTF.setStyle(errorStyle);
                throw new ControledException("Debe seleccionar un usuario de la tabla",
                        "UserController - delete");
            } else if (info("¿Está seguro de que quiere eliminar este usuario?", true) == InfoController.DialogResult.ACCEPT) {
                if (adminS.delete(userTable.getSelectionModel().getSelectedItem())) {
                    info("El Usuario fue eliminado con éxito", false);
                    refreshTable(userTable, adminS.findAll());
                    if (AdminService.getAdminLog() == null) {
                        ManagerUtil.reload();
                    }
                } else {
                    throw new Exception("El usuario no pudo ser borrado");
                }
            }
        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
        }
        reset();
    }

    /**
     * Metodo que controla la seleccion de una fila en la tabla, para no
     * permitir modificar el usuario administrador
     */
    @FXML
    private void selected() {
        if (userTable.getSelectionModel().getSelectedItem().getId() == 1) {
            modUserNameTF.setDisable(true);
            modUserPassTF.setDisable(true);
            modButton.setDisable(true);
            delButton.setDisable(true);
        } else {
            modUserNameTF.setDisable(false);
            modUserPassTF.setDisable(false);
            modButton.setDisable(false);
            delButton.setDisable(false);
        }

        modUserNameTF.setText(userTable.getSelectionModel().getSelectedItem().getUsername());
        delUserNameTF.setText(userTable.getSelectionModel().getSelectedItem().getUsername());

        resetStyle();
    }

    @FXML
    private void addUserNameTyped() {
        resetStyle();
    }

    @FXML
    private void addUserPassTyped() {
        resetStyle();
    }

    @FXML
    private void modUserNameTyped() {
        resetStyle();
    }

    @FXML
    private void modUserPassTyped() {
        resetStyle();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        idColumn.setCellValueFactory(new PropertyValueFactory<Admin, Long>("id"));
        userColumn.setCellValueFactory(new PropertyValueFactory<Admin, String>("username"));

        userTable.setItems(FXCollections.observableList(adminS.findAll()));
    }

    /**
     * Metodo que resetea los fields cuando algun caso de uso se completa
     */
    private void reset() {
        addUserNameTF.setText("");
        addUserPassTF.setText("");

        modUserNameTF.setText("");
        modUserPassTF.setText("");

        userTable.getSelectionModel().clearSelection();
        delUserNameTF.setText("");
    }

    private void resetStyle() {
        addUserNameTF.setStyle("");
        addUserPassTF.setStyle("");
        modUserNameTF.setStyle("");
        modUserPassTF.setStyle("");
        delUserNameTF.setStyle("");
    }

}
