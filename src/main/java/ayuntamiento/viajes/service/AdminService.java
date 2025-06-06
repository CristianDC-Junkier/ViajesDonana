package ayuntamiento.viajes.service;

import ayuntamiento.viajes.common.BackupUtil;
import ayuntamiento.viajes.common.LoggerUtil;
import ayuntamiento.viajes.common.SecurityUtil;
import ayuntamiento.viajes.dao.AdminDAO;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.Admin;
import java.io.IOException;

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
    private static Admin adminLog;

    static {
        adminDAO = new AdminDAO();
        adminLog = new Admin();
        adminLog.setId(1);
    }

    /**
     * Funcion para recojer el usuario
     *
     * @return El usuario logeado
     */
    public static Admin getAdminLog() {
        return adminLog;
    }

    public static void setAdminLog(Admin adminLog) {
        AdminService.adminLog = adminLog;
    }

    /**
     * Metodo que guarda un usuario en la base de datos, se controla con
     * SecurityUtil la contraseña, creando un hash y guardandola
     *
     * @param entity el usuario que pasa a ser guardado
     * @return el usuario creado con el id
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public Admin save(Admin entity) throws IOException, InterruptedException {
        Admin result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername()));
        if (userExists) {
            return null;
        }
        entity.setContraseña(entity.getPassword());
        result = (Admin) adminDAO.save(entity);
        adminList.add(result);
        return result;
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
    public Admin modify(Admin entity) throws SQLException, IOException, InterruptedException {
        Admin result;
        boolean userExists = adminList.stream()
                .anyMatch(user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId());
        if (userExists) {
            return null;
        }
        entity.setContraseña(entity.getPassword());
        result = (Admin) adminDAO.modify(entity, entity.getId());
        for (int i = 0; i < adminList.size(); i++) {
            if (adminList.get(i).getId() == entity.getId()) {
                adminList.set(i, entity);
            }
        }
        if (result.getId() == adminLog.getId()) {
            adminLog = result;
        }
        return result;
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
    public Admin modifyProfile(Admin entity) throws SQLException, IOException, InterruptedException {
        Admin result;
        boolean userExists;
        userExists = adminList.stream()
                .anyMatch((user -> user.getUsername().equals(entity.getUsername())
                && user.getId() != entity.getId()));
        if (userExists) {
            return null;
        }
        entity.setContraseña(entity.getPassword());
        result = (Admin) adminDAO.modify(entity, entity.getId());
        adminLog = result;
        return result;
    }

    public boolean delete(Admin entity) throws SQLException, IOException, InterruptedException {
        boolean deleted;
        deleted = adminDAO.delete(entity.getId());
        if (deleted) {
            if (adminLog.getId() == entity.getId()) {
                adminLog = null;
            }
            adminList.remove(entity);
        }
        return deleted;
    }

    public List<Admin> findAll() {
        return adminList;
    }

    public void rechargeList() throws IOException, InterruptedException {
        adminList = adminDAO.findAll();

    }

}
