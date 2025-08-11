package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.LoginDAO;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.Admin;
import ayuntamiento.viajes.model.Department;
import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Cristian
 */
public class LoginService {

    private static final LoginDAO loginDAO;
    private static String secret_Token;
    private static Admin adminLog;

    static {
        loginDAO = new LoginDAO();
    }

    public static String getSecret_token() {
        return secret_Token;
    }

    public static void setSecret_token(String secret_token) {
        LoginService.secret_Token = secret_token;
    }

    /**
     * Funcion para recoger el usuario
     *
     * @return El usuario logeado
     */
    public static Admin getAdminLog() {
        return adminLog;
    }

    public static void setAdminLog(Admin adminLog) {
        LoginService.adminLog = adminLog;
    }

    /**
     * Funcion para recoger el departamento del usuario
     *
     * @return El usuario logeado
     */
    public static long getAdminDepartment() {
        return adminLog.getDepartment();
    }

    public void login(String username, String password) throws LoginException, Exception {
        JsonNode result = loginDAO.login(username, password);
        adminLog = new Admin();
        adminLog.setUsername(username);
        adminLog.setPassword(password);
        adminLog.setId(result.get("id").asLong());
        adminLog.setDepartment(result.get("department").asLong());
        LoginService.secret_Token = result.get("token").asText();
    }

    public static boolean relog() throws LoginException, Exception {
        boolean logueado = false;
        LoginService.secret_Token
                = loginDAO.login(adminLog.getUsername(), adminLog.getPassword()).get("token").asText();
        return logueado;
    }

}
