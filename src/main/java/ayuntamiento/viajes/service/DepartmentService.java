package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.DepartmentDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Department;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Cristian Delgado Cruz
 * @since 2025-08-08
 * @version 1.0
 */
public class DepartmentService {
    
    private static final DepartmentDAO departmentDAO;
    private static List<Department> departmentList;
    
    static{
        departmentDAO = new DepartmentDAO();
    }

    public List<Department> findAll() {
        return departmentList;
    }
    
    public Optional<Department> findById(int department) {
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
                throw new ControledException(apiE.getMessage(), "TravellerService - " + method);
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
