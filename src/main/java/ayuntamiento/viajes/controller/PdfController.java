package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.service.PDFService;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

/**
 * FXML Controller class
 *
 * @author Cristian
 */
public class PdfController extends BaseController implements Initializable {

    @FXML
    private ChoiceBox boxPDF;
    @FXML
    private TextField namePDF;
    @FXML
    private TextField dirPDF;

    private final static PDFService pdf;

    private static final String INVALID_FILENAME_CHARS = "[\\\\/:*?\"<>|]";
    private static final String DATE_FORMAT = PropertiesUtil.getProperty("DATE_FORMAT");
    private static final DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

    static {
        pdf = new PDFService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        boxPDF.getItems().addAll("Todos","Notificados" , "Propio", "Alquilado");
        boxPDF.getSelectionModel().selectFirst();

        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Downloads");
        dirPDF.setText(defaultDir.getAbsolutePath());

        namePDF.setText("Informe-Vehiculos-"
                + LocalDate.now().format(dateformatter));
    }

    @FXML
    private void choosefolderPDF() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecciona una carpeta");

        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Downloads");

        if (defaultDir.exists()) {
            chooser.setInitialDirectory(defaultDir);
        }

        File selectedDirectory = chooser.showDialog(dirPDF.getScene().getWindow());
        if (selectedDirectory != null) {
            dirPDF.setStyle("");
            dirPDF.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void printPDF() {

        if (isNotValidFileName()) {
            error("El nombre del archivo PDF no puede contener carácteres especiales");
            namePDF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        } else if (isNotValidFolderName()) {
            error("La Carpeta elegida no existe");
            dirPDF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        } else {
            String typeSelected = (String) boxPDF.getValue();
            switch (typeSelected) {
                case "Todos" -> pdf.printAll(namePDF.getText(), dirPDF.getText());
                case "Notificados" -> pdf.printNotification(namePDF.getText(), dirPDF.getText());
                default -> pdf.printType(namePDF.getText(), dirPDF.getText(), typeSelected);
            }
        }
    }

    @FXML
    private void nameChangePDF() {
        namePDF.setStyle("");
    }

    @FXML
    private void dirChangePDF() {
        dirPDF.setStyle("");
    }

    private boolean isNotValidFileName() {
        return namePDF.getText().matches(".*" + INVALID_FILENAME_CHARS + ".*");
    }

    private boolean isNotValidFolderName() {
        String folderPath = dirPDF.getText();
        Path path = Paths.get(folderPath);
        return !Files.exists(path) || !Files.isDirectory(path);
    }

}
