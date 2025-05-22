package ayuntamiento.viajes.app;

import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.dao.SQLiteDataBase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.image.Image;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        try {
            /*Log*/
            LoggerUtil.iniLog();
            System.out.println("");

            /*Base de datos*/
            SQLiteDataBase.create();
            SQLiteDataBase.getConnection();
            SQLiteDataBase.initialize();

            scene = new Scene(loadFXML("/ayuntamiento/viajes/view/login"), 900, 700);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));
            stage.setTitle(" Viajes - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setRoot(String fxml, String title) throws IOException {
        scene.setRoot(loadFXML("/ayuntamiento/viajes/view/" + fxml));
        
        // Obtener la ventana actual y actualizar el título
        Stage stage = (Stage) scene.getWindow();
        stage.setTitle(" Viaje - " + title);

    }
    
    /**
     * Introduce las nuevas dimensiones de la escena
     *
     * @param width el ancho de la ventana
     * @param height la altura de la ventana
     * @throws IOException Si hay un error al cargar la vista
     */
    public static void setDim(int width, int height) throws IOException {
        Stage stage = (Stage) scene.getWindow();
        stage.setWidth(width);
        stage.setHeight(height);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class
                .getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {

        // Inicio de la App JavaFX
        launch();
    }

}
