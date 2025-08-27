package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.WorkerDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.model.Worker;
import java.util.ArrayList;

import java.util.List;

/**
 * Clase que se encarga de dar a los controladores acceso a los administradores
 * siendo el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class WorkerService {

    private static final WorkerDAO adminDAO;
    private static List<Worker> adminList;

    static {
        adminDAO = new WorkerDAO();
        adminList = new ArrayList<>();
    }

    /**
     * Metodo que guarda un administrador en la base de datos, se controla con
     * SecurityUtil la contraseña.
     *
     * @param entity el usuario que pasa a ser guardado
     * @return el administrador creado con el id
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Worker save(Worker entity) throws ControledException, Exception {
        return save(entity, true);
    }

    private Worker save(Worker entity, boolean allowRetry) throws ControledException, Exception {
        Worker result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername()));
        if (userExists) {
            return null;
        }
        entity.setPassword(entity.getPassword());
        try {
            result = (Worker) adminDAO.save(entity);
            adminList.add(result);
            return result;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "save");
            return null;
        }
    }

    /**
     * Metodo que modifica y guarda en la base de datos el administrador, se
     * controla con SecurityUtil la contraseña.
     *
     * @param entity el usuario que pasa a ser modificado
     * @return el usuario modificado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Worker modify(Worker entity) throws ControledException, Exception {
        return modify(entity, true);
    }

    private Worker modify(Worker entity, boolean allowRetry) throws ControledException, Exception {
        Worker result;

        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }

        entity.setPassword(entity.getPassword());
        try {
            result = (Worker) adminDAO.modify(entity, entity.getId());

            for (int i = 0; i < adminList.size(); i++) {
                if (adminList.get(i).getId() == entity.getId()) {
                    adminList.set(i, result);
                }
            }

            if (result.getId() == LoginService.getAdminLog().getId()) {
                LoginService.setAdminLog(result);
            }

            return result;

        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "modify");
            return null;
        }
    }

    /**
     * Metodo que modifica y guarda en la base de datos el administrador desde
     * su perfil, se controla con SecurityUtil la contraseña.
     *
     * @param entity el admin logeado con los datos modificados
     * @return el administrador modificado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Worker modifyProfile(Worker entity) throws ControledException, Exception {
        return modifyProfile(entity, true);
    }

    private Worker modifyProfile(Worker entity, boolean allowRetry) throws ControledException, Exception {
        Worker result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());

        if (userExists) {
            return null;
        }

        entity.setPassword(entity.getPassword());

        try {
            result = (Worker) adminDAO.modify(entity, entity.getId());
            LoginService.setAdminLog(result);
            adminList = null;
            return result;

        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "modifyProfile");
            return null;
        }
    }

    /**
     * Metodo que elimina un administrador de la base de datos.
     *
     * @param entity el admin logeado con los datos modificados
     * @return si fue o no eliminado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public boolean delete(Worker entity) throws ControledException, Exception {
        return delete(entity, true);
    }

    private boolean delete(Worker entity, boolean allowRetry) throws ControledException, Exception {
        boolean deleted;

        try {
            deleted = adminDAO.delete(entity.getId());

            if (deleted) {
                if (LoginService.getAdminLog().getId() == entity.getId()) {
                    LoginService.setAdminLog(null);
                }
                adminList.remove(entity);
            }

            return deleted;

        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "delete");
            return false;
        }
    }

    public List<Worker> findAll() {
        return adminList;
    }

    public static void rechargeList() throws ControledException, Exception {
        rechargeList(true);
    }

    private static void rechargeList(boolean allowRetry) throws ControledException, Exception {
        try {
            adminList = adminDAO.findAll();
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "rechargeList");
        }
    }

    /**
     * Metodo que utilizamos para comprobar que tipo de error hubo
     *
     * @param apiE Excepción que se llama
     * @param allowRetry Booleano que indica si hay o no un reintento, por fallo
     * de token
     * @param method método que lo invocó
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    private static void errorHandler(APIException apiE, boolean allowRetry, String method) throws ControledException, QuietException, Exception {
        switch (apiE.getStatusCode()) {
            case 400, 404 -> {
                rechargeList(false);
                throw new ControledException(apiE.getMessage(), "AdminService - " + method);
            }
            case 401 -> {
                if (allowRetry) {
                    LoginService.relog();
                } else {
                    throw new Exception(apiE.getMessage());
                }
            }
            case 204 -> {
                throw new QuietException(apiE.getMessage(), "AdminService - " + method);
            }
            default ->
                throw new Exception(apiE.getMessage());
        }
    }

}
