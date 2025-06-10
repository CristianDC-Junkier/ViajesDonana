package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.model.Traveller.TravellerTrip;
import ayuntamiento.viajes.service.PDFService;
import ayuntamiento.viajes.service.TravellerService;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

/**
 * Clase que se encarga del control de la vista de creación de pdfs así como de
 * comprobar que la carpeta donde se va a crear, y el nombre del fichero, son
 * correctos.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-04
 * @version 1.1
 */
public class PdfController extends BaseController implements Initializable {

    @FXML
    private ChoiceBox sortCB;
    @FXML
    private ChoiceBox tripTypeCB;
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

        sortCB.getItems().addAll("Nombre", "Viaje", "Fecha de Inscripción", "Lugar de Inscripción", "DNI");
        sortCB.getSelectionModel().selectFirst();

        tripTypeCB.getItems().add("Todos");
        tripTypeCB.getItems().addAll(Arrays.asList(Traveller.TravellerTrip.values()));
        tripTypeCB.getSelectionModel().selectFirst();
        tripTypeCB.setOnAction((event) -> {
            changePDFName(tripTypeCB.getSelectionModel().getSelectedItem().toString());
        });

        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Downloads");
        dirPDF.setText(defaultDir.getAbsolutePath());

        namePDF.setText("Listado-Viajes-"
                + LocalDate.now().format(dateformatter));
    }

    /**
     * Metodo que permite al usuario elegir una carpeta para colocar los pdf
     * creados
     */
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

    /**
     * Metodo que imprime el pdf, o muestra el error según donde el usuario se
     * equivocó
     */
    @FXML
    private void printPDF() {

        TravellerService travellerS = new TravellerService();

        if (isNotValidFileName()) {
            namePDF.setStyle(errorStyle);
            error(new ControledException("El nombre del archivo PDF no puede contener carácteres especiales",
                    "PDFController - printPDF"));
        } else if (isNotValidFolderName()) {
            dirPDF.setStyle(errorStyle);
            error(new ControledException("La carpeta elegida no existe",
                    "PDFController - printPDF"));
        } else {
            try {

                String typeSelected = toEnumCompatible(tripTypeCB.getValue().toString());
                
                switch (typeSelected) {
                    case "Todos" -> {
                        if (travellerS.findAll().isEmpty()) {
                            info("No existen viajeros registrados", false);
                        } else {
                            pdf.printAll(namePDF.getText(), dirPDF.getText(), sortCB.getValue().toString());
                        }
                    }
                    default -> {
                        if (travellerS.findByTrip(TravellerTrip.valueOf(typeSelected).ordinal()).isEmpty()) {
                            info("No existen viajeros registrados para el viaje " + typeSelected, false);
                        } else {
                            pdf.printType(namePDF.getText(), dirPDF.getText(), typeSelected, sortCB.getValue().toString());
                        }
                    }

                }

            } catch (Exception ex) {
                error(ex);
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


    /**
     * Cambia el nombre del PDF automáticamente
     */
    @FXML
    private void changePDFName(String extra) {
        if ("Todos".equals(extra)) {
            namePDF.setText("Listado-Viajes-"
                    + LocalDate.now().format(dateformatter));
        } else {
            namePDF.setText("Listado-Viajes-" + toEnumCompatible(extra) + "-"
                    + LocalDate.now().format(dateformatter));
        }
    }

    private boolean isNotValidFileName() {
        return namePDF.getText().matches(".*" + INVALID_FILENAME_CHARS + ".*");
    }

    private boolean isNotValidFolderName() {
        String folderPath = dirPDF.getText();
        Path path = Paths.get(folderPath);
        return !Files.exists(path) || !Files.isDirectory(path);
    }
    
    private String toEnumCompatible(String s){
        return s.replace(' ', '_');
    }

}
