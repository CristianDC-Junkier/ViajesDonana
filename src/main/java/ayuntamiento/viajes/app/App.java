package ayuntamiento.viajes.app;

import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.dao.SQLiteDataBase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * Main de la Applicación, tiene los metodos para iniciar cambiar el tamaño de
 * pantalla y cambiar de pantalla
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-05
 * @version 1.4
 */
public class App extends Application {

    private static Scene scene;

    /**
     * Metodo override de Application, se encarga de iniciar la aplicación para
     * ello cre el log, y la base de datos si no existiera, además de
     * inicializarla comienza con el login y en caso de un error lo indica en el
     * Log
     *
     * @since 2025-05-05
     * @param stage el stage es estatico de toda la app, este parametro es para
     * poder hacer override
     */
    @Override
    public void start(Stage stage) {

        try {
            /*Log*/
            LoggerUtil.iniLog();

            /*Base de datos*/
            SQLiteDataBase.create();
            SQLiteDataBase.getConnection();
            SQLiteDataBase.initialize();

            /*FXML inicial*/
            scene = new Scene(loadFXML("/ayuntamiento/viajes/view/login"), 1000, 800);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));
            stage.setTitle(" Viajes Doñana - Login");
            stage.setScene(scene);
            setDim(1000, 800);
            stage.show();

        } catch (IOException ioE) {
            LoggerUtil.log("Error al iniciar la aplicación - "
                    + "Error en la busqueda de los FXML, o la creación "
                    + "de los archivos la BD: \n" + ioE.getMessage() + "\n"
                    + ioE.getCause());
        } catch (SQLException sqE) {
            LoggerUtil.log("Error al iniciar la aplicación - "
                    + "Error en la inicialización de la BD: \n" + sqE.getMessage()
                    + "\n" + sqE.getCause());
        } catch (Exception ex) {
            LoggerUtil.log("Error al iniciar la aplicación - "
                    + "Error no documentado: \n" + ex.getMessage() + "\n"
                    + ex.getCause());
        }
    }

    /**
     * Cambia el fxml que se muestra
     *
     * @param fxml nueva página fxml
     * @param title titulo de la ventana
     * @throws IOException Si hay un error al cargar la vista
     */
    public static void setRoot(String fxml, String title) throws IOException {
        scene.setRoot(loadFXML("/ayuntamiento/viajes/view/" + fxml));

        /*Get the actual stage, and change the tittle*/
        Stage stage = (Stage) scene.getWindow();
        stage.setTitle(" Viajes Doñana - " + title);
    }

    /**
     * Introduce las nuevas dimensiones de la escena y la mueve al centro
     * si hay un cambio
     *
     * @param width el ancho de la ventana
     * @param height la altura de la ventana
     * @throws IOException Si hay un error al modificar la vista
     */
    public static void setDim(int width, int height) throws IOException {
        Stage stage = (Stage) scene.getWindow();

        if (!stage.isFullScreen() && !stage.isMaximized()) {
            double newWidth = width + 16;
            double newHeight = height + 40;

            boolean sizeChanged = stage.getWidth() != newWidth || stage.getHeight() != newHeight;

            if (sizeChanged) {
                stage.setWidth(newWidth);
                stage.setHeight(newHeight);

                /* Esperar a que se apliquen los cambios antes de centrar */
                Platform.runLater(() -> {
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                    stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
                    stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
                });
            }
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class
                .getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {

        launch();
    }

}
