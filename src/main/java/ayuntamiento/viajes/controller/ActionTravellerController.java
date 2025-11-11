package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ChoiceBoxUtil;
import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.model.Travel;
import ayuntamiento.viajes.model.Traveller;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.LoginService;
import ayuntamiento.viajes.service.TravelService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

/**
 * Clase que se encarga de la pestaña de añadir y modificar viajeros, es un
 * dialog modal, que depende de TravellerController
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-02
 * @version 1.3
 */
public class ActionTravellerController implements Initializable {

    private final static TravelService travelS;
    private final static DepartmentService departmentS;

    private static Traveller tSelected;
    private static Traveller tResult;

    private Stage dialogStage;

    @FXML
    private Label titleLabel;
    @FXML
    private TextField dniTF;
    @FXML
    private TextField nameTF;
    @FXML
    private TextField phoneTF;
    @FXML
    private ChoiceBox<Department> departmentCB;
    @FXML
    private ChoiceBox<Travel> tripCB;
    @FXML
    private DatePicker sign_upDP;

    @FXML
    private StackPane birthdaySP;
    @FXML
    private DatePicker birthdayDP;
    @FXML
    private CheckBox minorChB;
    @FXML
    private CheckBox govChB;

    @FXML
    private Button actionButton;

    private static int typeAction;

    private static final String SHOW_DATE_FORMAT = PropertiesUtil.getProperty("SHOW_DATE_FORMAT");
    private static final DateTimeFormatter formatter_Show_Date = DateTimeFormatter.ofPattern(SHOW_DATE_FORMAT);

    static {
        travelS = new TravelService();
        departmentS = new DepartmentService();
    }

    @FXML
    private void dniChange() {
        dniTF.setStyle("");
    }

    @FXML
    private void nameChange() {
        nameTF.setStyle("");
    }

    @FXML
    private void phoneChange() {
        phoneTF.setStyle("");
    }

    @FXML
    private void signupChange() {
        sign_upDP.setStyle("");
    }

    @FXML
    private void extract() {

        if (!checkFields(minorChB.selectedProperty().getValue()) || !checkDates()) {
            return;
        }
        tResult = new Traveller();

        if (govChB.selectedProperty().getValue()) {
            String dateTravel = tripCB.getValue().getDescriptor().substring(0, tripCB.getValue().getDescriptor().indexOf("-"));
            String dni = dniTF.getText().toUpperCase() + "-" + dateTravel;
            tResult.setDni(dni.trim());
        } else if (minorChB.selectedProperty().getValue()) {
            tResult.setDni(nameTF.getText().toUpperCase().trim() + "-" + birthdayDP.getValue());
        } else {
            tResult.setDni(dniTF.getText().toUpperCase().trim());
        }
        tResult.setName(nameTF.getText().toUpperCase().trim());
        tResult.setPhone(phoneTF.getText().trim());
        tResult.setDepartment(departmentCB.getValue().getId());
        tResult.setTrip(tripCB.getValue().getId());

        tResult.setSignUpDate(sign_upDP.getValue());

        if (typeAction == 1) {
            tResult.setId(tSelected.getId());
            tResult.setVersion(tSelected.getVersion());
        }

        dialogStage.close();
    }

    private Traveller getResult() {
        return tResult;
    }

    private void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private Stage getDialogStage() {
        return dialogStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        birthdaySP.setVisible(false);
        birthdaySP.setManaged(false);

        Pattern allowed = Pattern.compile("[0-9+ ]*");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return allowed.matcher(newText).matches() ? change : null;
        };
        TextFormatter<String> formatter = new TextFormatter<>(filter);
        phoneTF.setTextFormatter(formatter);

        // Carga departamentos desde DepartmentService
        String role = LoginService.getAccountDepartmentLog().getName();
        if (role != null && (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Superadmin"))) {
            // Carga departamentos desde DepartmentService
            List<Department> departments = departmentS.findAll().stream()
                    .filter(d -> !d.getName().equalsIgnoreCase("Admin") && !d.getName().equalsIgnoreCase("Superadmin"))
                    .toList();
            departmentCB.getItems().setAll(departments);
            departmentCB.setValue(departmentCB.getItems().get(0));

        } else {
            departmentCB.getItems().add(LoginService.getAccountDepartmentLog());
            departmentCB.setValue(departmentCB.getItems().get(0));
            departmentCB.setDisable(true);
            departmentCB.setMouseTransparent(true);
            ChoiceBoxUtil.setDisableArrow(departmentCB);
        }
        ChoiceBoxUtil.setDepartmentNameConverter(departmentCB);

        // Carga viajes desde TravelService
        List<Travel> travels = travelS.findByDepartment(departmentCB.getSelectionModel().getSelectedItem().getId());

        // Ordenar por fecha
        travels.sort((t1, t2) -> {
            LocalDate d1 = null, d2 = null;
            try {
                if (t1.getDescriptor() != null && !t1.getDescriptor().isEmpty()) {
                    d1 = LocalDate.parse(t1.getDescriptor().split("-")[0], formatter_Show_Date);
                }
                if (t2.getDescriptor() != null && !t2.getDescriptor().isEmpty()) {
                    d2 = LocalDate.parse(t2.getDescriptor().split("-")[0], formatter_Show_Date);
                }
            } catch (Exception e) {
                return 0;
            }

            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return -1;
            }
            if (d2 == null) {
                return 1;
            }
            return d1.compareTo(d2);
        });

        tripCB.getItems().setAll(travels);
        tripCB.setValue(travels.isEmpty() ? null : tripCB.getItems().getFirst());

        ChoiceBoxUtil.setTravelConverter(tripCB);

        /*Cambiar el titulo y los labels dependiendo del tipo*/
        if (typeAction == 0) {
            actionButton.setText("Añadir");
            titleLabel.setText("Añadir Viajero");
            sign_upDP.setValue(LocalDate.now());
        } else {
            actionButton.setText("Modificar");
            titleLabel.setText("Modificar Viajero");
            populateFields();
        }

        departmentCB.setOnAction((event) -> {
            List<Travel> trips = travelS.findByDepartment(departmentCB.getSelectionModel().getSelectedItem().getId());
            tripCB.getItems().setAll(trips);
            tripCB.setValue(trips.isEmpty() ? null : tripCB.getItems().getFirst());
        });
    }

    /**
     * Metodo que llama el controlador de Traveller recoge el viajero
     * seleccionado si fuera modificar el tipo que le llega, siendo 0 = añadir y
     * 1 = modificar y el vista/escena que lo llama
     *
     * @param parent vista/escena que lo llama
     * @param selected traveller que se modifica, en otro caso, null
     * @param type 0 si es añadir, 1 si es modificar
     *
     * @return traveller que se añadirá, o que se modificará
     * @throws Exception errores en la carga del dialogo
     */
    public static Traveller showActionTraveller(Stage parent, Traveller selected, int type) throws Exception {
        try {
            typeAction = type;
            tResult = null;
            tSelected = selected;

            FXMLLoader loader = new FXMLLoader(ActionTravellerController.class.getResource("/ayuntamiento/viajes/view/actiontraveller.fxml"));
            StackPane page = loader.load();
            ActionTravellerController actionController = loader.getController();

            actionController.setDialogStage(new Stage());
            actionController.getDialogStage().initModality(javafx.stage.Modality.APPLICATION_MODAL);
            actionController.getDialogStage().initOwner(parent);
            actionController.getDialogStage().getIcons().add(new Image(ErrorController.class.getResourceAsStream("/ayuntamiento/viajes/icons/icon-ayunt.png")));

            if (typeAction == 0) {
                actionController.getDialogStage().setTitle("Viajes Doñana - Añadir Viajante");
            } else {
                actionController.getDialogStage().setTitle("Viajes Doñana - Modificar Viajante");
            }

            /*Establecer la escena y mostrar el diálogo*/
            Scene scene = new Scene(page, 700, 600);
            actionController.getDialogStage().setScene(scene);
            actionController.getDialogStage().showAndWait();

            return actionController.getResult();

        } catch (Exception e) {
            throw new Exception("Error al cargar el diálogo para añadir o modificar viajantes");
        }
    }

    private void populateFields() {

        String dni = tSelected.getDni() != null ? tSelected.getDni().toUpperCase().trim() : "";
        String displayValue = dni;
        
        // Resetear checkboxes
        minorChB.setSelected(false);
        govChB.setSelected(false);

        if (!dni.isEmpty() && dni.contains("-")) {
            displayValue = dni.substring(0, dni.indexOf("-")).trim().toUpperCase();

            // Validar si es un DNI/NIE válido
            boolean esDniNieValido = displayValue.matches("^[0-9]{8}[A-Za-z]$") // DNI
                    || displayValue.matches("^[XYZxyz][0-9]{7}[A-Za-z]$");       // NIE

            if (!esDniNieValido) {
                // Es menor
                minorChB.setSelected(true);
                minorCheck(); // Ajusta birthdaySP y dniTF
                // Extra: poner la fecha de cumpleaños si está en el DNI
                String[] parts = dni.split("-");
                if (parts.length > 1) {
                    try {
                        birthdayDP.setValue(LocalDate.parse(parts[1])); // Ajustar formato si necesario
                    } catch (Exception ignored) {
                    }
                }
            } else {
                // Es gov
                govChB.setSelected(true);
                govCheck(); // Ajusta birthdaySP y dniTF
            }
        }
       dniTF.setText(displayValue);
        nameTF.setText(tSelected.getName());
        phoneTF.setText(tSelected.getPhone());
        departmentCB.setValue(departmentS.findById(tSelected.getDepartment()).get());
        List<Travel> trips = travelS.findByDepartment(departmentCB.getSelectionModel().getSelectedItem().getId());
        tripCB.getItems().setAll(trips);
        tripCB.setValue(travelS.findById(tSelected.getTrip()).get());
        sign_upDP.setValue(tSelected.getSignUpDate());

    }

    private boolean checkFields(boolean minor) {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);";
        String okStyle = ""; // o el estilo por defecto

        // Validar DNI
        if (!minor) {
            if (SecurityUtil.checkBadOrEmptyString(dniTF.getText())
                    || !SecurityUtil.checkDNI_NIE(dniTF.getText())) {
                dniTF.setStyle(errorStyle);
                correct = false;
            } else {
                dniTF.setStyle(okStyle);
            }
        }

        // Validar nombre
        if (SecurityUtil.checkBadOrEmptyString(nameTF.getText())) {
            nameTF.setStyle(errorStyle);
            correct = false;
        } else {
            nameTF.setStyle(okStyle);
        }

        // Validar teléfono: mínimo 9 cifras y formato internacional opcional
        String phone = phoneTF.getText().replaceAll("\\s+", "");
        if (!phone.matches("(\\+\\d{1,3})?\\d{9}")) {
            phoneTF.setStyle(errorStyle);
            correct = false;
        } else {
            if (!phone.startsWith("+")) {
                phone = "+34" + phone;
            }
            phone = phone.replaceFirst("(\\+\\d{1,3})(\\d{9})", "$1 $2");
            phoneTF.setText(phone);

            phoneTF.setStyle(okStyle);
        }

        return correct;
    }

    private boolean checkDates() {
        boolean correct = true;
        String errorStyle = "-fx-background-color: linear-gradient(from 0% 0% to 110% 110%, #e52d27, #b31217);";
        String okStyle = ""; // o el estilo por defecto

        if (!isValidDateFormat(sign_upDP, formatter_Show_Date)) {
            correct = false;
            sign_upDP.setStyle(errorStyle);
        } else {
            sign_upDP.setStyle(okStyle);
        }

        if (minorChB.selectedProperty().getValue()) {
            if (!isValidDateFormat(birthdayDP, formatter_Show_Date)) {
                correct = false;
                birthdayDP.setStyle(errorStyle);
            } else {
                birthdayDP.setStyle(okStyle);
            }
        }

        return correct;
    }

    private boolean isValidDateFormat(DatePicker datePicker, DateTimeFormatter formatter) {
        if (datePicker.getValue() != null) {
            formatter.format(datePicker.getValue());
            return true;
        }
        return false;
    }

    @FXML
    private void minorCheck() {
        if (minorChB.selectedProperty().getValue()) {
            birthdaySP.setVisible(true);
            birthdaySP.setManaged(true);
            dniTF.setDisable(true);
            govChB.selectedProperty().set(false);
        } else {
            dniTF.setDisable(false);
            birthdaySP.setVisible(false);
            birthdaySP.setManaged(false);
        }
    }

    @FXML
    private void govCheck() {
        if (govChB.selectedProperty().getValue()) {
            birthdaySP.setVisible(false);
            birthdaySP.setManaged(false);
            minorChB.selectedProperty().set(false);
        }
    }

}
