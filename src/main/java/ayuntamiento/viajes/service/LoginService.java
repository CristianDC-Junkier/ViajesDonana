package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.LoginDAO;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.Worker;
import ayuntamiento.viajes.model.Department;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Clase que se encarga de dar a los controladores acceso a los departamentos
 * siendo el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-12
 * @version 1.0
 */
public class LoginService {

    private static final LoginDAO loginDAO;
    private static String secret_Token;
    private static Worker adminLog;
    private static Department departmentLog;

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
    public static Worker getAccountLog() {
        return adminLog;
    }

    public static void setAccountLog(Worker adminLog) {
        LoginService.adminLog = adminLog;
    }

    /**
     * Funcion para recoger el departamento del usuario
     *
     * @return El departamento del usuario
     */
    public static Department getAccountDepartmentLog() {
        return departmentLog;
    }

    public static void setAccountDepartmentLog(Department departmentLog) {
        LoginService.departmentLog = departmentLog;
    }

    public void login(String username, String password) throws LoginException, Exception {
        JsonNode result = loginDAO.login(username, password);
        adminLog = new Worker();
        adminLog.setUsername(username);
        adminLog.setPassword(password);
        adminLog.setId(result.get("id").asLong());
        adminLog.setDepartment(result.get("departmentId").asLong());

        departmentLog = new Department();
        departmentLog.setId(result.get("departmentId").asLong());
        departmentLog.setName(result.get("departmentName").asText());
        departmentLog.setAdminId(result.get("id").asLong());

        LoginService.secret_Token = result.get("token").asText();
    }

    public static void relog() throws LoginException, Exception {
        LoginService.secret_Token
                = loginDAO.login(adminLog.getUsername(), adminLog.getPassword()).get("token").asText();
        System.out.println(LoginService.secret_Token);
    }

}
