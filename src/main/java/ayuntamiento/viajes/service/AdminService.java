package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.AdminDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Admin;

import java.util.List;

/**
 * Clase que se encarga de dar a los controladores acceso a los administradores
 * siendo el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class AdminService {

    private static final AdminDAO adminDAO;
    private static List<Admin> adminList;

    static {
        adminDAO = new AdminDAO();
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
    public Admin save(Admin entity) throws ControledException, Exception {
        return save(entity, true);
    }

    private Admin save(Admin entity, boolean allowRetry) throws ControledException, Exception {
        Admin result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername()));
        if (userExists) {
            return null;
        }
        entity.setPassword(entity.getPassword());
        try {
            result = (Admin) adminDAO.save(entity);
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
    public Admin modify(Admin entity) throws ControledException, Exception {
        return modify(entity, true);
    }

    private Admin modify(Admin entity, boolean allowRetry) throws ControledException, Exception {
        Admin result;

        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }

        entity.setPassword(entity.getPassword());
        try {
            result = (Admin) adminDAO.modify(entity, entity.getId());

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
    public Admin modifyProfile(Admin entity) throws ControledException, Exception {
        return modifyProfile(entity, true);
    }

    private Admin modifyProfile(Admin entity, boolean allowRetry) throws ControledException, Exception {
        Admin result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());

        if (userExists) {
            return null;
        }

        entity.setPassword(entity.getPassword());

        try {
            result = (Admin) adminDAO.modify(entity, entity.getId());
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
    public boolean delete(Admin entity) throws ControledException, Exception {
        return delete(entity, true);
    }

    private boolean delete(Admin entity, boolean allowRetry) throws ControledException, Exception {
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

    public List<Admin> findAll() {
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
    private static void errorHandler(APIException apiE, boolean allowRetry, String method) throws ControledException, Exception {
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
            default ->
                throw new Exception(apiE.getMessage());
        }
    }

}
