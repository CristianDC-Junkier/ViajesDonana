package ayuntamiento.viajes.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import ayuntamiento.viajes.service.UserService;
import ayuntamiento.viajes.model.User;
import javafx.scene.control.PasswordField;

/**
 * 
 * @author Cristian
 * @since 2025-05-19
 * @version 1.0
 */
public class ProfileController extends BaseController implements Initializable {

    UserService userS;

    @FXML
    private TextField nameProfile;
    @FXML
    private PasswordField passwordProfile;

    public ProfileController() {
        userS = new UserService();
    }

    @FXML
    private void modify() {
        if (nameProfile.getText().isBlank()) {
            nameProfile.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        } else if (passwordProfile.getText().isBlank()) {
            passwordProfile.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        } else if (nameProfile.getText().length() > 16) {
            nameProfile.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
            error("El nombre no debe contener más de 16 carácteres");
        } else if (passwordProfile.getText().length() > 16) {
            passwordProfile.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
            error("La contraseña no debe contener más de 16 carácteres");
        } else {
            User u = new User(0, nameProfile.getText(), passwordProfile.getText());
            u.setId(UserService.getUsuarioLog().getId());
            if (userS.modifyProfile(u) == null) {
                error("El nombre de usuario ya existe");
            } else {
                reset();
                //quiza explicarlo al usuario con una pantalla
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();
        nameProfile.setText(UserService.getUsuarioLog().getUsername());
    }

    private void reset() {
        nameProfile.setText("");;
        passwordProfile.setText("");
    }

}
