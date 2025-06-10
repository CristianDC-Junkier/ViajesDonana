package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.AdminDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Admin;

import java.sql.SQLException;
import java.util.List;

/**
 * Clase que se encarga de dar a los controladores acceso a los usuarios siendo
 * el servicio especifico de ello.
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
     * Metodo que guarda un usuario en la base de datos, se controla con
     * SecurityUtil la contraseña, creando un hash y guardandola
     *
     * @param entity el usuario que pasa a ser guardado
     * @return el usuario creado con el id
     * @throws ControledException
     * @throws Exception
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
        entity.setContraseña(entity.getPassword());
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
     * Metodo que modifica y guarda en la base de datos el usuario se controla
     * con SecurityUtil la contraseña, creando un hash y guardandola
     *
     * @param entity el usuario que pasa a ser modificado
     * @return el usuario modificado
     * @throws SQLException si hubo algun fallo en la modificación
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    // Método público sin el parámetro allowRetry
    public Admin modify(Admin entity) throws Exception {
        return modify(entity, true); // permite un reintento por defecto
    }

    // Método privado con control de reintento
    private Admin modify(Admin entity, boolean allowRetry) throws Exception {
        Admin result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }

        entity.setContraseña(entity.getPassword());
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
     * Metodo que modifica y guarda en la base de datos el usuario desde su
     * perfil se controla con SecurityUtil la contraseña, creando un hash y
     * guardandola
     *
     * @param entity el usuario logeado con los datos modificados
     * @return el usuario modificado
     * @throws SQLException si hubo algun fallo en la modificación
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    // Método público sin parámetro, permite reintento por defecto
    public Admin modifyProfile(Admin entity) throws Exception {
        return modifyProfile(entity, true);
    }

    // Método privado con control de reintento
    private Admin modifyProfile(Admin entity, boolean allowRetry) throws Exception {
        Admin result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());

        if (userExists) {
            return null;
        }

        entity.setContraseña(entity.getPassword());

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

    // Método público con reintento por defecto
    public boolean delete(Admin entity) throws Exception {
        return delete(entity, true);
    }

    // Método privado con control de reintento
    private boolean delete(Admin entity, boolean allowRetry) throws Exception {
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

    // Método público con reintento por defecto
    public static void rechargeList() throws Exception {
        rechargeList(true);
    }

    // Método privado con control de reintento
    private static void rechargeList(boolean allowRetry) throws Exception {
        try {
            adminList = adminDAO.findAll();
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "rechargeList");
        }
    }

    private static void errorHandler(APIException apiE, boolean allowRetry, String method) throws ControledException, Exception {
        switch (apiE.getStatusCode()) {
            case 400, 404 ->
                throw new ControledException(apiE.getMessage(), "AdminService - " + method);
            case 401 -> {
                if (allowRetry) {
                    LoginService.relog();
                    rechargeList(false);
                } else {
                    throw new Exception(apiE.getMessage());
                }
            }
            default ->
                throw new Exception(apiE.getMessage());
        }
    }

}
