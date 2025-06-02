package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.PreferencesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.Preferences;
import ayuntamiento.viajes.model.User;
import ayuntamiento.viajes.service.UserService;
import ayuntamiento.viajes.service.VehicleService;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;

/**
 * Clase que controla la creación el login de los usuarios y actua como vista
 * inicial
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

    /**
     * Intenta crear al usuario, en caso de error se coloca un mensaje en la
     * parte inferior del login
     */
    @FXML
    public void login() {
        try {
            usuarioS.rechargeList();
            if (SecurityUtil.checkBadOrEmptyString(userField.getText())) {
                log("Error en el login, el usuario no es válido");
                wrongUser(1000, "El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            } else if (SecurityUtil.checkBadOrEmptyString(passField.getText())) {
                log("Error en el login, la contraseña no es válida");
                wrongUser(2000, "La contaseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            } else {
                try {
                    User userlog = usuarioS.findByCredentials(userField.getText(), passField.getText());
                    rememberUser(userlog);
                    setVehicles();
                    ManagerUtil.moveTo("home");
                } catch (SQLException sqlE) {
                    error(new Exception(sqlE));
                }

            }
        } catch (LoginException lE) {
            wrongUser(lE.getErrorCode(), lE.getMessage());
        } catch (Exception ex) {
            error(ex);
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
        String username = PreferencesUtil.getPreferences().getRemember();

        if (username != null) {
            rememberCheck.setSelected(true);
            userField.setText(username);
        }
    }

    /**
     * Recarga por primera vez los vehiculos
     */
    private void setVehicles() throws SQLException {
        VehicleService vS = new VehicleService();
        if (vS.findAll() == null) {
            vS.rechargeList();
        }

    }

    public void wrongUser(int error, String msg) {
        if (error == 1000) {
            log("Error en el login, " + msg);
            userField.setStyle(errorStyle);
        } else if (error == 2000) {
            log("Error en el login, " + msg);
            passField.setStyle(errorStyle);
        }
        userField.setText("");
        passField.setText("");
        errorlabel.setText(msg);
    }

    public void rememberUser(User userlog) {
        Preferences pref = new Preferences();

        if (rememberCheck.isSelected()) {
            pref.setRemember(userlog.getUsername());
        }

        PreferencesUtil.setPreferences(pref);
    }
}
