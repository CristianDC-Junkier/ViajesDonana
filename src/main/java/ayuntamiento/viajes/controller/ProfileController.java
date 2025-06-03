package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.exception.ControledException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import ayuntamiento.viajes.service.AdminService;
import ayuntamiento.viajes.model.Admin;
import javafx.scene.control.PasswordField;

/**
 * Clase controladora que se encarga del funcionamiento de la pestaña del perfil
 * del usuario, no administrador, logeado.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-19
 * @version 1.1
 */
public class ProfileController extends BaseController implements Initializable {

    AdminService userS;

    @FXML
    private TextField nameProfileTF;
    @FXML
    private PasswordField passwordProfileTF;

    private final int numMaxChars = 16;

    public ProfileController() {
        userS = new AdminService();
    }

    /**
     * Metodo FXML que modifica el usuario que lo llama. Controla que no se
     * envíe un nombre o clave de un tamaño superior a 16 carácteres, ni tampoco
     * vacio, colocando el field en rojo si algo falla además también comprueba
     * que el usuario esta de acuerdo antes de eliminarlo.
     */
    @FXML
    private void modify() {
        try {
            if (SecurityUtil.checkBadOrEmptyString(nameProfileTF.getText())) {
                nameProfileTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "ProfileController - modify");
            } else if (SecurityUtil.checkBadOrEmptyString(passwordProfileTF.getText())) {
                passwordProfileTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "ProfileController - modify");

            } else if (nameProfileTF.getText().length() > numMaxChars) {
                nameProfileTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe contener más de 16 carácteres ",
                        "ProfileController - modify");

            } else if (passwordProfileTF.getText().length() > numMaxChars) {
                passwordProfileTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe contener más de 16 carácteres ",
                        "ProfileController - modify");
            } else {
                if (info("¿Está seguro de que quiere modificar su usuario?", true)
                        == InfoController.DialogResult.REJECT) {
                    reset();
                } else {
                    Admin a = new Admin();
                    a.setUsername(nameProfileTF.getText());
                    a.setContraseña(passwordProfileTF.getText());
                    a.setId(AdminService.getAdminLog().getId());

                    if (userS.modifyProfile(a) == null) {
                        throw new ControledException("El nombre de usuario ya existe", "ProfileController - modify");
                    } else {
                        reset();
                        info("Su usuario ha sido modificado", false);
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

    @FXML
    private void nameProfileChange() {
        nameProfileTF.setStyle("");
    }

    @FXML
    private void passwordProfileChange() {
        passwordProfileTF.setStyle("");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();
        nameProfileTF.setText(AdminService.getAdminLog().getUsername());
    }

    private void reset() {
        nameProfileTF.setText("");
        passwordProfileTF.setText("");
    }

}
