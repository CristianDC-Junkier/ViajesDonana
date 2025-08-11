package ayuntamiento.viajes.controller;

import static ayuntamiento.viajes.common.LoggerUtil.log;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.PreferencesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.common.TaskExecutorUtil;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.LoginService;
import ayuntamiento.viajes.service.TravelService;
import ayuntamiento.viajes.service.TravellerService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Clase que controla la creación el login de los usuarios y actua como vista
 * inicial
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-03
 * @version 1.0
 */
public class LoginController extends BaseController implements Initializable {

    LoginService loginS;

    @FXML
    private StackPane father;
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
    @FXML
    private ImageView backgroundIV;

    /**
     * Intenta crear al usuario, en caso de error se coloca un mensaje en la
     * parte inferior del login
     */
    @FXML
    private void login() {
        changePI();

        String username = userField.getText();
        String password = passField.getText();

        if (SecurityUtil.checkBadOrEmptyString(username)) {
            log("Error en el login, el usuario no es válido");
            wrongUser(1000, "El nombre no debe estar vacío ni contener los siguientes caracteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            changePI();
            return;
        }

        if (SecurityUtil.checkBadOrEmptyString(password)) {
            log("Error en el login, la contraseña no es válida");
            wrongUser(2000, "La contraseña no debe estar vacía ni contener los siguientes caracteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            changePI();
            return;
        }

        /**
         * Ejecuta el login en segundo plano para no molestar a la interfaz
         *
         */
        TaskExecutorUtil.runAsync(
                () -> {
                    loginS.login(username, password);
                    return null;
                },
                result -> {
                    changePI();
                    try {
                        try {
                            DepartmentService.rechargeList();
                        } catch (QuietException qE) {
                            error(qE);
                        }
                        try {
                            TravelService.rechargeList();
                        } catch (QuietException qE) {
                            error(qE);
                        }
                        try {
                            TravellerService.rechargeList();
                        } catch (QuietException qE) {
                            error(qE);
                        }
                        ManagerUtil.moveTo("home");
                    } catch (ControledException cE) {
                        error(cE);
                    } catch (Exception ex) {
                        error(ex);
                    }
                },
                error -> {
                    changePI();
                    if (error instanceof LoginException le) {
                        wrongUser(le.getErrorCode(), le.getMessage());
                    } else {
                        error((Exception) error);
                    }
                }
        );

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
        backgroundIV.fitWidthProperty().bind(father.widthProperty());
        backgroundIV.fitHeightProperty().bind(father.heightProperty());

        loginS = new LoginService();
        String username = PreferencesUtil.getPreferences().getRemember();

        if (username != null) {
            rememberCheck.setSelected(true);
            userField.setText(username);
        }
    }

    private void wrongUser(int error, String msg) {
        switch (error) {
            case 1000 -> {
                log("Error en el login, " + msg);
                userField.setStyle(errorStyle);
            }
            case 2000 -> {
                log("Error en el login, " + msg);
                passField.setStyle(errorStyle);
            }
            case 401 -> {
                log("Error en el login, " + msg);
                userField.setStyle(errorStyle);
                passField.setStyle(errorStyle);
            }
            default ->
                log("Error en el login, " + msg);
        }
        userField.setText("");
        passField.setText("");
        errorlabel.setText(msg);
    }

    private void changePI() {
        chargePI.setVisible(!chargePI.isVisible());
        chargePI.setFocusTraversable(!chargePI.isFocusTraversable());
    }

}
