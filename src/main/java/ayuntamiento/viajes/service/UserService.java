package ayuntamiento.viajes.service;

import ayuntamiento.viajes.common.BackupUtil;
import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.dao.UserDAO;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.User;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lo unico realizado de esta clase es save y findbycredentials
 *
 * @author Cristian
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

    public static User getUsuarioLog() {
        return userLog;
    }

    public static void setUsuarioLog(User usuarioLog) {
        UserService.userLog = usuarioLog;
    }

    public User save(User entity) {
        User result;
        boolean userExists = usersList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername()));
        if (userExists) {
            return null;
        }
        //Hash de contraseña
        entity.setContraseña(SecurityUtil.hashPassword(entity.getPassword()));
        result = userDAO.save(entity);
        usersList.add(result);
        return result;
    }

    public User modify(User entity) {
        User result;
        boolean userExists = usersList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }
        //Hash de contraseña
        entity.setContraseña(SecurityUtil.hashPassword(entity.getPassword()));
        result = userDAO.modify(entity);
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getId() == entity.getId()) {
                usersList.set(i, entity);
            }
        }
        if(result.getId() == userLog.getId()){
            userLog = result;
        }
        return result;
    }

    public User modifyProfile(User entity) {
        User result;
        boolean userExists = userDAO.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }
        //Hash de contraseña
        entity.setContraseña(SecurityUtil.hashPassword(entity.getPassword()));
        result = userDAO.modify(entity);
        userLog = result;
        return result;
    }

    public boolean delete(User entity) {
        boolean deleted = userDAO.delete(entity);
        if (deleted) {
            usersList.remove(entity);
        }
        return deleted;
    }

    public User findByCredentials(String user, String password) throws LoginException {
        User credential = usersList.stream()
                .filter(userF -> userF.getUsername().equals(user))
                .findFirst()
                .orElse(null);
        if (credential == null) {
            LoggerUtil.log("Error en el login, el usuario no fue encontrado");
            throw new LoginException("El nombre de usuario no ha sido encontrado", 1000);
        } else {
            if (!SecurityUtil.verifyPassword(password, credential.getPassword())) {
                LoggerUtil.log("Error en el login, la contraseña no es valida");
                throw new LoginException("La Contraseña no es valida", 2000);
            } else {
                userLog = credential;
                if (userLog.getType().ordinal() == 0) {
                    usersList = null;
                }
                LoggerUtil.log("Usuario conectado Conectado");
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

    public void rechargeList() {
        usersList = userDAO.findAll();
    }

}
