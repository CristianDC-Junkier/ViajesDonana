package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.service.LoginService;
import ayuntamiento.viajes.service.TravellerService;
import java.awt.Desktop;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controlador de la vista principal, FALTA
 *
 * @author Cristian
 * @since 2025-06-03
 * @version 1.0
 */
public class HomeController extends BaseController implements Initializable {

    @FXML
    private ImageView sunIV;
    @FXML
    private ImageView stadisticsIV;
    @FXML
    private Label welcomeLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();
        welcomeLabel.setText("¡ Bienvenido a la aplicación, " + LoginService.getAdminLog().getUsername() + " !");
    }

    @FXML
    private void travellerspanel() throws IOException {
        try {
            TravellerService.rechargeList();
            ManagerUtil.moveTo("traveller");
        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML
    private void stadisticspanel() throws IOException {
        try {
            TravellerService.rechargeList();
            ManagerUtil.moveTo("stadistics");
        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML
    private void pdfspanel() throws IOException {
        try {
            TravellerService.rechargeList();
            ManagerUtil.moveTo("pdf");
        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML
    private void openManual() {
        try (InputStream input = getClass().getResourceAsStream("/ayuntamiento/viajes/manuals/Manual_de_Usuario.pdf")) {

            if (input == null) {
                throw new Exception("El PDF no fue encontrado en los recursos internos");
            }

            File tempFile = File.createTempFile("Manual_de_Usuario", ".pdf");
            tempFile.deleteOnExit();

            Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                throw new ControledException("El PDF no pudo ser abierto automáticamente. "
                        + "Revise la carpeta de instalación", "HomeController - openManual");
            }

        } catch (ControledException cE) {
            error(cE);
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML
    private void travellergif() {
        sunIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_sun.gif").toExternalForm()));
    }

    @FXML
    private void travellerpng() {
        sunIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_sun.png").toExternalForm()));
    }

    @FXML
    private void stadisticsgif() {
        stadisticsIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_stadistics.gif").toExternalForm()));
    }

    @FXML
    private void stadisticspng() {
        stadisticsIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_stadistics.png").toExternalForm()));
    }

}
