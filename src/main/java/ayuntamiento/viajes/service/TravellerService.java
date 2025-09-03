package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.TravellerDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.model.Traveller;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que se encarga de dar a los controladores acceso a los viajeros siendo
 * el servicio especifico de ello.
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-04
 * @version 1.3
 */
public class TravellerService {

    private static final TravellerDAO travellerDAO;
    private static List<Traveller> travellerList;

    static {
        travellerDAO = new TravellerDAO();
        travellerList = new ArrayList<>();
    }

    /**
     * Metodo que guarda un traveller en la base de datos, se controla con
     * SecurityUtil la contraseña.
     *
     * @param entity el traveller que pasa a ser guardado
     * @return el traveller creado con el id
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Traveller save(Traveller entity) throws ControledException, Exception {
        return save(entity, true);
    }

    private Traveller save(Traveller entity, boolean allowRetry) throws ControledException, Exception {
        Traveller result;
        boolean travellerExists = travellerList.stream()
                .anyMatch(traveller -> traveller.getDni().equalsIgnoreCase(entity.getDni()));
        if (travellerExists) {
            return null;
        }
        try {
            result = (Traveller) travellerDAO.save(entity);
            travellerList.add(result);
            return result;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "save");
            return null;
        }
    }

    /**
     * Metodo que modifica y guarda en la base de datos el traveller, se
     * controla con SecurityUtil la contraseña.
     *
     * @param entity el traveller que pasa a ser modificado
     * @return el traveller modificado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Traveller modify(Traveller entity) throws Exception {
        return modify(entity, true);
    }

    public Traveller modify(Traveller entity, boolean allowRetry) throws Exception {
        // Evitar duplicados por DNI en otro viajero
        boolean exists = travellerList.stream()
                .anyMatch(t -> t.getDni().equalsIgnoreCase(entity.getDni())
                && t.getId() != entity.getId());
        if (exists) {
            return null;
        }
        try {
            Traveller updated = (Traveller) travellerDAO.modify(entity, entity.getId());
            travellerList.replaceAll(t -> t.getId() == entity.getId() ? updated : t);
            return updated;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "modify");
            return null;
        }
    }

    /**
     * Metodo que elimina un traveller de la base de datos.
     *
     * @param entity el traveller logeado con los datos modificados
     * @return si fue o no eliminado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public boolean delete(Traveller entity) throws Exception {
        return delete(entity, true);
    }

    public boolean delete(Traveller entity, boolean allowRetry) throws Exception {
        boolean deleted;
        try {
            deleted = travellerDAO.delete(entity.getId());
            travellerList.remove(entity);
            return deleted;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "delete");
            return false;
        }
    }

    public List<Traveller> findAll() {
        return travellerList;
    }

    public Traveller findById(long id) {
        return travellerList.stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Traveller> findByTrip(long trip) {
        return travellerList.stream()
                .filter(v -> v.getTrip() == trip)
                .collect(Collectors.toList());
    }

    public List<Traveller> findByDepartment(long department) {
        return travellerList.stream()
                .filter(v -> v.getDepartment() == department)
                .collect(Collectors.toList());
    }

    public static void rechargeList() throws Exception {
        rechargeList(true);
    }

    public static void rechargeList(boolean allowRetry) throws IOException, InterruptedException, Exception {
        try {
            String role = LoginService.getAdminDepartment().getName();
            if (role != null && (role.equalsIgnoreCase("Admin") || role.equalsIgnoreCase("Superadmin"))) {
                travellerList = travellerDAO.findAll();
            } else {
                travellerList = travellerDAO.findByDepartment(LoginService.getAdminDepartment().getId());
            }
        } catch (APIException apiE) {
            if (apiE.getStatusCode() == 204) {
                travellerList = new ArrayList();
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
            case 400, 404, 409 -> {
                if (allowRetry) {
                    rechargeList(false);
                }
                throw new ControledException(apiE.getMessage(), "TravellerService - " + method);
            }
            case 401 -> {
                if (allowRetry) {
                    LoginService.relog();
                } else {
                    throw new Exception(apiE.getMessage());
                }
            }
            case 204 -> {
                throw new QuietException(apiE.getMessage(), "TravellerService - " + method);
            }
            default ->
                throw new Exception(apiE.getMessage());
        }
    }

}
