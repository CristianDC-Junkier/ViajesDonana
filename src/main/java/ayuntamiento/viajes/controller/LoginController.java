package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.PreferencesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.User;
import ayuntamiento.viajes.service.UserService;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;

/**
 * Clase que controla la creación el log de los usuarios y actua como vista
 * principal
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-14
 * @version 1.2
 */
public class LoginController extends BaseController implements Initializable {

    UserService usuarioS;

    @FXML
    private TextField userField;
    @FXML
    private PasswordField passField;
    @FXML
    private Label errorlabel;
    @FXML
    private CheckBox rememberCheck;

    @FXML
    public void login() {
        try {
            usuarioS.rechargeList();
            if (SecurityUtil.checkBadString(userField.getText())) {
                LoggerUtil.log("Error en el login, el usuario no fue encontrado");
                wrongUser(1000, "El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            } else if (SecurityUtil.checkBadString(passField.getText())) {
                LoggerUtil.log("Error en el login, la contraseña no es valida");
                wrongUser(2000, "La contaseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            } else {
                User userlog = usuarioS.findByCredentials(userField.getText(), passField.getText());
                rememberUser(userlog);
                ManagerUtil.moveTo("home");
            }
        } catch (LoginException ex) {
            wrongUser(ex.getErrorCode(), ex.getMessage());
        } catch (IOException ex) {
            LoggerUtil.log("Error del login, no se pudo entrar en la vista home");
            System.out.println("Fallo al entrar en el Home");
        }

    }

    @FXML
    public void userPressed() {
        userField.setStyle("");
    }

    @FXML
    public void passPressed() {
        passField.setStyle("");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioS = new UserService();

        if (!PreferencesUtil.getRemember().equalsIgnoreCase("")) {
            rememberCheck.setSelected(true);
        }
        userField.setText(PreferencesUtil.getRemember());
    }

    public void wrongUser(int error, String msg) {
        if (error == 1000) {
            userField.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        } else if (error == 2000) {
            passField.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }
        userField.setText("");
        passField.setText("");
        errorlabel.setText(msg);
    }

    public void rememberUser(User userlog) {
        if (rememberCheck.isSelected()) {
            PreferencesUtil.setRemember(userlog.getUsername());
        } else {
            PreferencesUtil.setRemember("");
        }
    }

}
