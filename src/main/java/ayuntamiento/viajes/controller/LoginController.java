package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.PreferencesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.Preferences;
import ayuntamiento.viajes.model.Admin;
import ayuntamiento.viajes.service.AdminService;
import ayuntamiento.viajes.service.TravellerService;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;

/**
 * Clase que controla la creación el login de los usuarios y actua como vista
 * inicial
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-14
 * @version 1.2
 */
public class LoginController extends BaseController implements Initializable {

    AdminService adminS;

    @FXML
    private ProgressIndicator chargePI;
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
    private void login() {
        try {
            changePI();
            adminS.rechargeList();
            if (SecurityUtil.checkBadOrEmptyString(userField.getText())) {
                log("Error en el login, el usuario no es válido");
                wrongUser(1000, "El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            } else if (SecurityUtil.checkBadOrEmptyString(passField.getText())) {
                log("Error en el login, la contraseña no es válida");
                wrongUser(2000, "La contaseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            } else {
                try {
                    Admin userlog = adminS.findByCredentials(userField.getText(), passField.getText());
                    rememberUser(userlog);
                    setTravellers();
                    changePI();
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
    private void userPressed() {
        userField.setStyle("");
    }

    @FXML
    private void passPressed() {
        passField.setStyle("");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        adminS = new AdminService();
        String username = PreferencesUtil.getPreferences().getRemember();

        if (username != null) {
            rememberCheck.setSelected(true);
            userField.setText(username);
        }
    }

    /**
     * Recarga por primera vez los vehiculos
     */
    private void setTravellers() throws SQLException {
        TravellerService vS = new TravellerService();
        if (vS.findAll() == null) {
            vS.rechargeList();
        }

    }

    private void wrongUser(int error, String msg) {
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

    private void rememberUser(Admin userlog) {
        Preferences pref = new Preferences();

        if (rememberCheck.isSelected()) {
            pref.setRemember(userlog.getUsername());
        }

        PreferencesUtil.setPreferences(pref);
    }
    
    private void changePI(){
        chargePI.setVisible(!chargePI.isVisible());
        chargePI.setFocusTraversable(!chargePI.isFocusTraversable());
    }
    
}
