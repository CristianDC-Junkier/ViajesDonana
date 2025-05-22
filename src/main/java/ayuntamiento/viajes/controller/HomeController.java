package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.service.NotificationService;
import ayuntamiento.viajes.service.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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
 * FXML Controller class
 *
 * @author Cristian
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
        setNotifications();
        welcomeLabel.setText("¡ Bienvenido a la aplicación, " + UserService.getUsuarioLog().getUsername() + " !");
    }

    @FXML
    private void vehiclespanel() throws IOException {
        ManagerUtil.moveTo("vehicle");
    }

    @FXML
    private void notificationspanel() throws IOException {
        ManagerUtil.moveTo("notification");
    }

    @FXML
    private void stadisticspanel() throws IOException {
        ManagerUtil.moveTo("stadistics");
    }

    @FXML
    private void pdfspanel() throws IOException {
        ManagerUtil.moveTo("pdf");
    }

    private void setNotifications() {
        NotificationService notificationS = new NotificationService();
        notificationS.rechargeNotifications();
        int n = notificationS.getNumberOfNotifications();

        if (n > 0) {
            notificationsCount.setText(String.valueOf(n));
            notificationsAlert.setVisible(true);
            setupGlowAnimation();
        }
    }

    private void setupGlowAnimation() {
        glowTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            double time = System.currentTimeMillis() % 6000; // ciclo de 6 segundos
            double progress = Math.abs(Math.sin(time / 6000.0 * Math.PI));

            // Radio animado más amplio para que se note
            double animatedRadius = 0.4 + 0.1 * progress; // 0.4 a 0.55

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

            // Animación de escala del círculo, entre 0.9 y 1.1
            double scale = 0.9 + 0.1 * progress;
            notificationsCircle.setScaleX(scale);
            notificationsCircle.setScaleY(scale);

        }));

        glowTimeline.setCycleCount(Timeline.INDEFINITE);
        glowTimeline.play();
    }

}
