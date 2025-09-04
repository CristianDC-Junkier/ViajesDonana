package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.DepartmentDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.exception.ReloadException;
import ayuntamiento.viajes.model.Department;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Clase que se encarga de dar a los controladores acceso a los departamentos
 * siendo el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-08-08
 * @version 1.0
 */
public class DepartmentService {

    private static final DepartmentDAO departmentDAO;
    private static List<Department> departmentList;

    static {
        departmentDAO = new DepartmentDAO();
        departmentList = new ArrayList<>();

    }

    public List<Department> findAll() {
        return departmentList;
    }

    public Optional<Department> findById(long department) {
        return departmentList.stream()
                .filter(d -> d.getId() == department)
                .findFirst();
    }

    public static void rechargeList() throws Exception {
        rechargeList(true);
    }

    public static void rechargeList(boolean allowRetry) throws IOException, InterruptedException, Exception {
        try {
            departmentList = departmentDAO.findAll();
        } catch (APIException apiE) {
            if (apiE.getStatusCode() == 204) {
                departmentList = new ArrayList();
            } else {
                errorHandler(apiE, allowRetry, "rechargeList");
            }
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
                throw new ControledException(apiE.getMessage(), "DepartamentService - " + method);
            }
            case 401 -> {
                if (allowRetry) {
                    try {
                        LoginService.relog();
                    } catch (Exception e) {
                        throw new ReloadException("Por seguridad, su sesión ha expirado. Inicie sesión de nuevo para continuar.", false);
                    }
                    throw new ReloadException("La sesión había expirado, pero ya está activa nuevamente.\n Por favor, realice otra vez la operación anterior.", true);
                } else {
                    throw new ReloadException("Por seguridad, su sesión ha expirado. Inicie sesión de nuevo para continuar.", false);
                }
            }
            case 204 -> {
                throw new QuietException(apiE.getMessage(), "DepartamentService - " + method);
            }
            default ->
                throw new Exception(apiE.getMessage());
        }
    }
}
