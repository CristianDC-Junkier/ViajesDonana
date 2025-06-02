package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.service.UserService;
import java.awt.Desktop;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Controlador de la vista principal, se encarga de cargar por primera vez la
 * lista de vehículos de la base de datos y de generar las notificaciones
 *
 * @author Cristian
 * @since 2025-05-09
 * @version 1.1
 */
public class HomeController extends BaseController implements Initializable {

    @FXML
    private StackPane notificationsAlert;
    @FXML
    private Label notificationsCount;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Circle notificationsCircle;

    private Timeline glowTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();
        welcomeLabel.setText("¡ Bienvenido a la aplicación, " + UserService.getUsuarioLog().getUsername() + " !");
    }

    @FXML
    private void vehiclespanel() throws IOException {
        ManagerUtil.moveTo("vehicle");
    }

    @FXML
    private void notificationspanel() throws IOException {
    }

    @FXML
    private void stadisticspanel() throws IOException {
        ManagerUtil.moveTo("stadistics");
    }

    @FXML
    private void pdfspanel() throws IOException {
        ManagerUtil.moveTo("pdf");
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

    /**
     * Metodo que coloca una animación "Glow" circular en el número de
     * notificaciones, para captar la atención.
     */
    private void setupGlowAnimation() {
        glowTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            double time = System.currentTimeMillis() % 4000;
            double progress = Math.abs(Math.sin(time / 4000.0 * Math.PI));

            /*Radio animado más amplio para que se note*/
            double animatedRadius = 0.4 + 0.1 * progress;

            RadialGradient gradient = new RadialGradient(
                    90,
                    0.0,
                    0.5, 0.4848,
                    animatedRadius,
                    true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.WHITE),
                    new Stop(0.0067, Color.WHITE),
                    new Stop(0.141, Color.WHITE),
                    new Stop(0.706, Color.web("#ff3b3b")),
                    new Stop(1.0, Color.web("#8b0000"))
            );

            notificationsCircle.setFill(gradient);

            /* Animación de escala del círculo, entre 0.9 y 1.1*/
            double scale = 0.9 + 0.1 * progress;
            notificationsCircle.setScaleX(scale);
            notificationsCircle.setScaleY(scale);

        }));

        glowTimeline.setCycleCount(Timeline.INDEFINITE);
        glowTimeline.play();
    }

}
