package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.LoginService;
import ayuntamiento.viajes.service.TravelService;
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
import javafx.scene.layout.VBox;

/**
 * Controlador de la vista principal, se encarga de hacer el traspaso a las
 * diferentes partes de la app, y actua de página de inicio
 *
 * @author Cristian
 * @since 2025-06-03
 * @version 1.0
 */
public class HomeController extends BaseController implements Initializable {

    @FXML
    private ImageView sunIV;
    @FXML
    private ImageView planeIV;
    @FXML
    private ImageView stadisticsIV;
    @FXML
    private VBox travelVB;
    @FXML
    private Label departmentLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        showTravelButton();
        departmentLabel.setText(LoginService.getAdminDepartment().getName().replace("_", " "));
    }

    @FXML
    private void travellerspanel() throws IOException {
        try {
            TravellerService.rechargeList();
            ManagerUtil.moveTo("traveller");
        } catch (ControledException cE) {
            error(cE);
        } catch (QuietException qE) {
            error(qE);
            ManagerUtil.moveTo("traveller");
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML
    private void travelspanel() throws IOException {
        try {
            TravelService.rechargeList();
            ManagerUtil.moveTo("travel");
        } catch (ControledException cE) {
            error(cE);
        } catch (QuietException qE) {
            error(qE);
            ManagerUtil.moveTo("travel");
        } catch (Exception ex) {
            error(ex);
        }
    }

    @FXML
    private void stadisticspanel() throws IOException {
        try {
            TravellerService.rechargeList();
            if (LoginService.getAdminDepartment().getName().equalsIgnoreCase("Admin")) {
                ManagerUtil.moveTo("stadistics_admin");
            } else {
                ManagerUtil.moveTo("stadistics_department");
            }
        } catch (ControledException cE) {
            error(cE);
        } catch (QuietException qE) {
            error(qE);
            ManagerUtil.moveTo("stadistics");
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
        } catch (QuietException qE) {
            error(qE);
            ManagerUtil.moveTo("pdf");
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
        } catch (QuietException qE) {
            error(qE);
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
    private void travelgif() {
        planeIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_sun.gif").toExternalForm()));
    }

    @FXML
    private void travelpng() {
        planeIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_sun.png").toExternalForm()));
    }

    @FXML
    private void stadisticsgif() {
        stadisticsIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_stadistics.gif").toExternalForm()));
    }

    @FXML
    private void stadisticspng() {
        stadisticsIV.setImage(new Image(getClass().getResource("/ayuntamiento/viajes/icons/home_stadistics.png").toExternalForm()));
    }

    /**
     * Oculta el boton de acceso al panel de viajes segun el usuario
     */
    private void showTravelButton() {
        if (LoginService.getAdminDepartment().getName().equalsIgnoreCase("Admin")) {
            travelVB.setVisible(true);
            travelVB.setManaged(true);
        } else {
            travelVB.setVisible(false);
            travelVB.setManaged(false);
        }
    }

}
