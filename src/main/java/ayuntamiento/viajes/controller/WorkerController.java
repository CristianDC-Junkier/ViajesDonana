package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.ChoiceBoxUtil;
import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.exception.ControledException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;

import ayuntamiento.viajes.service.WorkerService;
import ayuntamiento.viajes.model.Worker;
import ayuntamiento.viajes.model.Department;
import ayuntamiento.viajes.service.DepartmentService;
import ayuntamiento.viajes.service.LoginService;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * Clase controladora que se encarga del funcionamiento de la pestaña de
 * administración de trabajadores.
 *
 * @author Ramón Iglesias Granados
 * @since 2025-06-03
 * @version 1.7
 */
public class WorkerController extends BaseController implements Initializable {

    private final static WorkerService workerS;
    private final static DepartmentService departmentS;

    @FXML
    private TableView<Worker> userTable;
    @FXML
    private TableColumn idColumn;
    @FXML
    private TableColumn userColumn;
    @FXML
    private TableColumn departmentColumn;

    @FXML
    private VBox addVBox;
    @FXML
    private TextField addUserNameTF;
    @FXML
    private TextField addUserPassTF;
    @FXML
    private ChoiceBox<Department> addUserDepCB;

    @FXML
    private TextField modUserNameTF;
    @FXML
    private TextField modUserPassTF;
    @FXML
    private ChoiceBox<Department> modUserDepCB;
    @FXML
    private Button modButton;

    @FXML
    private TextField delUserNameTF;
    @FXML
    private Button delButton;

    private final int numMaxChars = 16;

    static {
        workerS = new WorkerService();
        departmentS = new DepartmentService();
    }

    /**
     * Metodo que añade un trabajador, controla que no se envie un nombre o
     * clave de un tamaño superior a 16 carácteres, ni tampoco vacio, colocando
     * el field en rojo si algo falla
     */
    @FXML
    private void add() {
        try {
            if (SecurityUtil.checkBadOrEmptyString(addUserNameTF.getText())) {
                addUserNameTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "WorkerController - add");

            } else if (SecurityUtil.checkBadOrEmptyString(addUserPassTF.getText())) {
                addUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contaseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "WorkerController - add");
            } else if (addUserNameTF.getText().length() > numMaxChars) {
                addUserPassTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe contener más de 16 carácteres",
                        "WorkerController - add");
            } else if (addUserPassTF.getText().length() > numMaxChars) {
                addUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe contener más de 16 carácteres",
                        "WorkerController - add");
            } else {
                Worker u = new Worker(addUserNameTF.getText(), addUserPassTF.getText(), addUserDepCB.getValue().getId());

                if (workerS.save(u) == null) {
                    addUserPassTF.setStyle(errorStyle);
                    throw new ControledException("El nombre de usuario ya existe: " + u.getUsername(),
                            "WorkerController - add");
                } else {
                    LoggerUtil.log("Usuario: " + u.getUsername() + " añadido correctamente");
                    refreshTable(userTable, workerS.findAll(),null);
                    reset();
                }
            }
        } catch (ControledException cE) {
            error(cE);
            refreshTable(userTable, workerS.findAll(),null);
        } catch (Exception ex) {
            error(ex);
            reset();
        }
    }

    /**
     * Metodo que modifica un trabajador, controla que no se envie un nombre o
     * clave de un tamaño superior a 16 carácteres, ni tampoco vacio, colocando
     * el field en rojo si algo falla
     */
    @FXML
    private void modify() {
        try {
            if (userTable.getSelectionModel().getSelectedItem() == null) {
                throw new ControledException("Debe seleccionar un usuario de la tabla",
                        "WorkerController - modify");
            } else if (SecurityUtil.checkBadOrEmptyString(modUserNameTF.getText())) {
                modUserNameTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "WorkerController - modify");
            } else if (SecurityUtil.checkBadOrEmptyString(modUserPassTF.getText())) {
                modUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>",
                        "WorkerController - modify");

            } else if (modUserNameTF.getText().length() > numMaxChars) {
                modUserNameTF.setStyle(errorStyle);
                throw new ControledException("El nombre no debe contener más de 16 carácteres ",
                        "WorkerController - modify");

            } else if (modUserPassTF.getText().length() > numMaxChars) {
                modUserPassTF.setStyle(errorStyle);
                throw new ControledException("La contraseña no debe contener más de 16 carácteres ",
                        "WorkerController - modify");
            } else {
                Worker u = new Worker(modUserNameTF.getText(), modUserPassTF.getText(), modUserDepCB.getValue().getId());
                u.setId(userTable.getSelectionModel().getSelectedItem().getId());

                Worker userMod = workerS.modify(u);
                if (userMod == null) {
                    modUserNameTF.setStyle(errorStyle);
                    throw new ControledException("El nombre de usuario ya existe: " + u.getUsername(), "UserController - modify");
                } else {
                    if (LoginService.getAdminLog().getId() == userMod.getId()) {
                        ManagerUtil.reload();
                    } else {
                        info("Usuario, " + userMod.getUsername() + ", modificado con éxito", false);
                        refreshTable(userTable, workerS.findAll(),null);
                        reset();
                    }
                }
            }
        } catch (ControledException cE) {
            error(cE);
            refreshTable(userTable, workerS.findAll(),null);
        } catch (Exception ex) {
            error(ex);
            reset();
        }
    }

    /**
     * Metodo que elimina un trabajador, controla que el usuario administrador,
     * preguntando si de verdad quieres hacerlo, colocando el field en rojo si
     * algo falla
     */
    @FXML
    private void delete() {
        try {
            if (userTable.getSelectionModel().getSelectedItem() == null) {
                delUserNameTF.setStyle(errorStyle);
                throw new ControledException("Debe seleccionar un usuario de la tabla",
                        "WorkerController - delete");
            } else if (info("¿Está seguro de que quiere eliminar este usuario?", true) == InfoController.DialogResult.ACCEPT) {
                if (workerS.delete(userTable.getSelectionModel().getSelectedItem())) {
                    info("El Usuario fue eliminado con éxito", false);
                    refreshTable(userTable, workerS.findAll(),null);
                    if (LoginService.getAdminLog() == null) {
                        ManagerUtil.reload();
                    }
                } else {
                    throw new Exception("El usuario no pudo ser borrado");
                }
            }
        } catch (ControledException cE) {
            error(cE);
            refreshTable(userTable, workerS.findAll(),null);
        } catch (Exception ex) {
            error(ex);
        }
        reset();
    }

    /**
     * Metodo que controla la seleccion de una fila en la tabla, para no
     * permitir modificar el usuario administrador
     */
    @FXML
    private void selected() {
        if (userTable.getSelectionModel().getSelectedItem() != null) {
            if (userTable.getSelectionModel().getSelectedItem().getId() == LoginService.getAdminLog().getId()) {
                modUserNameTF.setDisable(true);
                modUserPassTF.setDisable(true);
                modButton.setDisable(true);
                delButton.setDisable(true);
                modUserDepCB.setDisable(true);
            } else {
                modUserNameTF.setDisable(false);
                modUserPassTF.setDisable(false);
                modButton.setDisable(false);
                delButton.setDisable(false);
                modUserDepCB.setDisable(false);
            }

            modUserNameTF.setText(userTable.getSelectionModel().getSelectedItem().getUsername());
            delUserNameTF.setText(userTable.getSelectionModel().getSelectedItem().getUsername());

            resetStyle();
        } else {
            modUserNameTF.setDisable(false);
            modUserPassTF.setDisable(false);
            modButton.setDisable(false);
            delButton.setDisable(false);
            modUserDepCB.setDisable(false);
            
            modUserNameTF.setText("");
            delUserNameTF.setText("");

            resetStyle();
        }
    }

    @FXML
    private void addUserNameTyped() {
        resetStyle();
    }

    @FXML
    private void addUserPassTyped() {
        resetStyle();
    }

    @FXML
    private void modUserNameTyped() {
        resetStyle();
    }

    @FXML
    private void modUserPassTyped() {
        resetStyle();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();

        // Limpia la selección al pulsar en algun sitio del crear
        addVBox.setOnMouseClicked(event -> {
            userTable.getSelectionModel().clearSelection();
            selected();
        });

        idColumn.setCellValueFactory(new PropertyValueFactory<Worker, Long>("id"));
        userColumn.setCellValueFactory(new PropertyValueFactory<Worker, String>("username"));
        //Callbacks para mostrar los departamentos y viajes por nombre en vez de por el ID
        departmentColumn.setCellValueFactory(new Callback<CellDataFeatures<Worker, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(CellDataFeatures<Worker, String> p) {
                return new SimpleStringProperty(departmentS.findById(p.getValue().getDepartment()).get().getName().replace('_', ' '));
            }
        });

        userTable.setItems(FXCollections.observableList(workerS.findAll()));

        // Carga departamentos desde DepartmentService
        List<Department> departments = departmentS.findAll();
        addUserDepCB.getItems().setAll(departments);
        addUserDepCB.setValue(addUserDepCB.getItems().get(0));
        ChoiceBoxUtil.setDepartmentNameConverter(addUserDepCB);

        modUserDepCB.getItems().setAll(departments);
        modUserDepCB.setValue(modUserDepCB.getItems().get(0));
        ChoiceBoxUtil.setDepartmentNameConverter(modUserDepCB);
    }

    /**
     * Metodo que resetea los fields cuando algun caso de uso se completa
     */
    private void reset() {
        addUserNameTF.setText("");
        addUserPassTF.setText("");

        modUserNameTF.setText("");
        modUserPassTF.setText("");

        userTable.getSelectionModel().clearSelection();
        delUserNameTF.setText("");
    }

    private void resetStyle() {
        addUserNameTF.setStyle("");
        addUserPassTF.setStyle("");
        modUserNameTF.setStyle("");
        modUserPassTF.setStyle("");
        delUserNameTF.setStyle("");
    }

}
