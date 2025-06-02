package ayuntamiento.viajes.service;

import ayuntamiento.viajes.common.BackupUtil;
import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.dao.UserDAO;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que se encarga de dar a los controladores acceso a los usuarios siendo
 * el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-05-09
 * @version 1.5
 */
public class UserService {

    private static final UserDAO userDAO;
    private static List<User> usersList;
    private static User userLog;

    static {
        userDAO = new UserDAO();
    }

    /**
     * Funcion para recojer el usuario 
     * 
     * @return El usuario logeado
     */
    public static User getUsuarioLog() {
        return userLog;
    }

    
    public static void setUsuarioLog(User usuarioLog) {
        UserService.userLog = usuarioLog;
    }

    /**
     * Metodo que guarda un usuario en la base de datos, 
     * se controla con SecurityUtil la contraseña, creando un hash y guardandola
     * 
     * @param entity el usuario que pasa a ser guardado
     * @return el usuario creado con el id
     * @throws SQLException si hubo algun fallo en guardando el usuario
     */
    public User save(User entity) throws SQLException {
        User result;
        boolean userExists = usersList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername()));
        if (userExists) {
            return null;
        }
        entity.setContraseña(SecurityUtil.hashPassword(entity.getPassword()));
        result = userDAO.save(entity);
        usersList.add(result);
        return result;
    }
    
    /**
     * Metodo que modifica y guarda en la base de datos el usuario 
     * se controla con SecurityUtil la contraseña, creando un hash y guardandola
     * 
     * @param entity el usuario que pasa a ser modificado
     * @return el usuario modificado
     * @throws SQLException si hubo algun fallo en la modificación
     */
    public User modify(User entity) throws SQLException {
        User result;
        boolean userExists = usersList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }
        entity.setContraseña(SecurityUtil.hashPassword(entity.getPassword()));
        result = userDAO.modify(entity);
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getId() == entity.getId()) {
                usersList.set(i, entity);
            }
        }
        if (result.getId() == userLog.getId()) {
            userLog = result;
        }
        return result;
    }

    /**
     * Metodo que modifica y guarda en la base de datos el usuario desde su perfil
     * se controla con SecurityUtil la contraseña, creando un hash y guardandola
     * 
     * @param entity el usuario logeado con los datos modificados
     * @return el usuario modificado
     * @throws SQLException si hubo algun fallo en la modificación
     */
    public User modifyProfile(User entity) throws SQLException {
        User result;
        boolean userExists;
        userExists = userDAO.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }
        entity.setContraseña(SecurityUtil.hashPassword(entity.getPassword()));
        result = userDAO.modify(entity);
        userLog = result;
        return result;
    }

    public boolean delete(User entity) throws SQLException {
        boolean deleted;
        deleted = userDAO.delete(entity);
        if (deleted) {
            if(userLog.getId() == entity.getId()){
                userLog = null;
            }
            usersList.remove(entity);
        }
        return deleted;
    }

    /**
     * Funcion para recojer el usuario  al logearse
     * 
     * @param user el nombre de usuario
     * @param password la contraseña plana
     * @exception LoginException Falla si hubiera un problema cuando se crea el usuario
     * @return El usuario logeado
     */
    public User findByCredentials(String user, String password) throws LoginException {
        User credential = usersList.stream()
                .filter(userF -> userF.getUsername().equals(user))
                .findFirst()
                .orElse(null);
        if (credential == null) {
            throw new LoginException("El nombre de usuario no ha sido encontrado", 1000);
        } else {
            if (!SecurityUtil.verifyPassword(password, credential.getPassword())) {
                throw new LoginException("La contraseña no es correcta", 2000);
            } else {
                userLog = credential;
                if (userLog.getType().ordinal() == 0) {
                    usersList = null;
                }
                LoggerUtil.log("Usuario conectado");
                BackupUtil.createBackup();
            }
        }
        return userLog;
    }

    public List<User> findByType(int type) {
        return usersList.stream()
                .filter(u -> u.getType().ordinal() == type)
                .collect(Collectors.toList());
    }

    public List<User> findAll() {
        return usersList;
    }

    public void rechargeList() throws SQLException {
        usersList = userDAO.findAll();

    }

}
