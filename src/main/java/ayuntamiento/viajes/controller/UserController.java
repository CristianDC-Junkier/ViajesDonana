package ayuntamiento.viajes.controller;

import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.ManagerUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;

import ayuntamiento.viajes.service.UserService;
import ayuntamiento.viajes.model.User;
import java.io.IOException;


/**
 * Clase controladora que se encarga del funcionamiento de la pestaña de 
 * administración de usuarios.
 *
 * @author Ramón Iglesias
 * @since 2025-05-09
 * @version 1.5
 */
public class UserController extends BaseController implements Initializable {

    private final UserService userS;
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn idColumn;
    @FXML private TableColumn typeColumn;
    @FXML private TableColumn userColumn;
    
    @FXML private TextField addUserNameTF;
    @FXML private TextField addUserPassTF;
    @FXML private CheckBox addUserAdminCheck;
    
    @FXML private TextField modUserNameTF;
    @FXML private TextField modUserPassTF;
    @FXML private CheckBox modUserAdminCheck;
    @FXML private Button modButton;
    
    @FXML private TextField delUserNameTF;
    @FXML private Button delButton;
    
    private final int numMaxChars = 16;
    
    
    public UserController(){
        userS = new UserService();
    }
    
    /**
    * Metodo FXML que añade un usuario
    * Controla que no se envie un nombre o clave
    * de un tamaño superior a 16 carácteres,
    * ni tampoco vacio, colocando el field
    * en rojo si algo falla
    */
    @FXML
    private void add(){
        if(SecurityUtil.checkBadString(addUserNameTF.getText())){
            LoggerUtil.log("Error al añadir un usuario, el nombre esta en blanco o utiliza un patrón sospechoso: " + addUserNameTF.getText());
            error("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            addUserNameTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(SecurityUtil.checkBadString(addUserPassTF.getText())){
            LoggerUtil.log("Error al añadir un usuario, la contraseña esta en blanco o utiliza un patrón sospechoso: " + addUserPassTF.getText());
            error("La contaseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            addUserPassTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(addUserNameTF.getText().length() > numMaxChars){
            LoggerUtil.log("Error al añadir un usuario, el nombre supera los 16 carácteres");
            error("El nombre no debe contener más de 16 carácteres");
            addUserPassTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(addUserPassTF.getText().length() > numMaxChars){
            LoggerUtil.log("Error al añadir un usuario, la contraseña supera los 16 carácteres");
            error("La contraseña no debe contener más de 16 carácteres");
            addUserPassTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else{
            User u = new User(0, addUserNameTF.getText(), addUserPassTF.getText());
             if(addUserAdminCheck.isSelected()) u.setTipo(1);
             if(userS.save(u) == null){
                 LoggerUtil.log("Error al añadir un usuario, ya existe ese nombre: " + u.getUsername());
                 error("El nombre de usuario ya existe");
                 addUserPassTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
             }
             else{
                 LoggerUtil.log("Usuario: " + u.getUsername() + " añadido correctamente");
                 refreshTable(userTable, userS.findAll());
                 reset(); 
             }
        }
    }
    
    /**
    * Metodo FXML que modifica un usuario
    * Controla que no se envie un nombre o clave
    * de un tamaño superior a 16 carácteres,
    * ni tampoco vacio, colocando el field
    * en rojo si algo falla
    */
    @FXML
    private void modify(){
        if(userTable.getSelectionModel().getSelectedItem() == null){
            error("Debe seleccionar un usuario de la tabla");
        }
        else if(SecurityUtil.checkBadString(modUserNameTF.getText())){
            LoggerUtil.log("Error al modificar un usuario, el nombre esta en blanco o utiliza un patrón sospechoso: " + modUserNameTF.getText());
            error("El nombre no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            modUserNameTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(SecurityUtil.checkBadString(modUserPassTF.getText())){
            LoggerUtil.log("Error al modificar un usuario, la contraseña esta en blanco o utiliza un patrón sospechoso: " + modUserNameTF.getText());
            error("La contraseña no debe estar vacía ni contener los siguientes carácteres: <--> , <;>, <'>, <\">, </*>, <*/>");
            modUserPassTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(modUserNameTF.getText().length() > numMaxChars){
            LoggerUtil.log("Error al modificar un usuario, el nombre supera los 16 carácteres");
            error("El nombre no debe contener más de 16 carácteres");
            modUserNameTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else if(modUserPassTF.getText().length() > numMaxChars){
            LoggerUtil.log("Error al modificar un usuario, la contraseña supera los 16 carácteres");
            error("La contraseña no debe contener más de 16 carácteres");
            modUserPassTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else{
            User u = new User(0, modUserNameTF.getText(), modUserPassTF.getText());
            u.setId(userTable.getSelectionModel().getSelectedItem().getId());
             if(modUserAdminCheck.isSelected()) u.setTipo(1);
             User userMod = userS.modify(u);
             if(userMod == null){
                 LoggerUtil.log("Error al añadir un usuario, ya existe ese nombre: " + u.getUsername());
                 error("El nombre de usuario ya existe");
             }
             else{
                if(UserService.getUsuarioLog().getId() == userMod.getId() 
                        && userMod.getType().ordinal() == 0){
                    try {
                        ManagerUtil.moveTo("profile");
                    } catch (IOException ex) {
                        LoggerUtil.log("Error al pasar a la vista de perfil desde el modificar el propio usuario"
                                + ", después de eliminar el estatus de administrador.");
                    }
                }
                else{
                    info("Usuario, " + userMod.getUsername() + ", modificado con éxito",false);
                    LoggerUtil.log("Usuario, " + userMod.getUsername() + ", modificado con éxito");
                    refreshTable(userTable, userS.findAll());
                    reset();
                   
                }
             }
        }
    }
    

    @FXML
    private void delete(){
        if(userTable.getSelectionModel().getSelectedItem() == null){
            error("Debe seleccionar un usuario de la tabla");
            delUserNameTF.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e52d27, #b31217);");
        }else
            if(info("¿Está seguro que quiere eliminar este usuario?",true) == InfoController.DialogResult.ACCEPT){
               if(userS.delete(userTable.getSelectionModel().getSelectedItem())){
                   LoggerUtil.log("Usuario: " + userTable.getSelectionModel().getSelectedItem().getUsername() + " eliminado");
                   info("El Usuario fue eliminado con éxito",false);
                   refreshTable(userTable, userS.findAll());
            }
            else{
                LoggerUtil.log("Error al borrar un usuario");
                error("Ocurrió un error al intentar eliminar el usuario seleccionado");
            }
        }
        reset(); 
    }
    
    /**
    * Metodo FXML que controla la seleccion
    * de una fila en la tabla, para no
    * permitir modificar el usuario administrador
    */
    @FXML
    private void selected(){
        if(userTable.getSelectionModel().getSelectedItem().getId() == 1){
            modUserNameTF.setDisable(true);
            modUserPassTF.setDisable(true);
            modUserAdminCheck.setDisable(true);
            modButton.setDisable(true);
            delButton.setDisable(true);
        }
        else{
            modUserNameTF.setDisable(false);
            modUserPassTF.setDisable(false);
            modUserAdminCheck.setDisable(false);
            modButton.setDisable(false);
            delButton.setDisable(false);
        }
        
        modUserNameTF.setText(userTable.getSelectionModel().getSelectedItem().getUsername());
        if(userTable.getSelectionModel().getSelectedItem().getType().ordinal() == 0) 
            modUserAdminCheck.setSelected(false); 
        else modUserAdminCheck.setSelected(true);
        delUserNameTF.setText(userTable.getSelectionModel().getSelectedItem().getUsername());
        
        resetStyle();
    }
    
    @FXML
    private void addUserNameTyped (){
         resetStyle();
    }
    @FXML
    private void addUserPassTyped (){
         resetStyle();
    }
    @FXML
    private void modUserNameTyped(){
         resetStyle();
    }
    @FXML
    private void modUserPassTyped(){
        resetStyle();
    }
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showUserOption();
        
        idColumn.setCellValueFactory(new PropertyValueFactory<User, Long>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<User, User.UserType>("type"));
        userColumn.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
        
        userTable.setItems(FXCollections.observableList(userS.findAll()));
    }   
    
    /**
    * Metodo que resetea los fields 
    * cuando algun caso de uso se completa
    */
    private void reset(){
        addUserNameTF.setText("");
        addUserPassTF.setText("");
        addUserAdminCheck.setSelected(false);
        
        modUserNameTF.setText("");
        modUserPassTF.setText("");
        modUserAdminCheck.setSelected(false);
        
        userTable.getSelectionModel().clearSelection();
        delUserNameTF.setText(""); 
    }
    
    private void resetStyle(){
        addUserNameTF.setStyle("");
        addUserPassTF.setStyle("");
        modUserNameTF.setStyle("");
        modUserPassTF.setStyle("");
        delUserNameTF.setStyle("");
    }
    
}
