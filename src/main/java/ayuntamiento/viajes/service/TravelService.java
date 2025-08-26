package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.TravelDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.exception.QuietException;
import ayuntamiento.viajes.model.Travel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Clase que se encarga de dar a los controladores acceso a los viajes
 * siendo el servicio especifico de ello.
 * 
 * @author Ramon Iglesias Granados
 * @since 2025-08-08
 * @version 1.0
 */
public class TravelService {

    private static final TravelDAO travelDAO;
    private static List<Travel> travelList;

    static {
        travelDAO = new TravelDAO();
        travelList = new ArrayList<>();
    }

    /**
     * Metodo que devuelve un mapa de viajes por el id y el descriptor de la
     * lista
     *
     * @return el mapa creado
     */
    public static Map<String, Travel> getTravelMapByDescriptorAndBus() {
        return travelList.stream()
                .collect(Collectors.toMap(
                        t -> t.getDescriptor() + " - Bus " + t.getBus(),
                        t -> t
                ));
    }

    /**
     * Metodo que guarda un travel en la base de datos, se controla con
     * SecurityUtil la contraseña.
     *
     * @param entity el travel que pasa a ser guardado
     * @return el travel creado con el id
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Travel save(Travel entity) throws ControledException, Exception {
        return save(entity, true);
    }

    public Travel save(Travel entity, boolean allowRetry) throws ControledException, Exception {
        Travel result;
        boolean travelExists = travelList.stream()
                .anyMatch(travel -> travel.getDescriptor().equalsIgnoreCase(entity.getDescriptor()));
        if (travelExists) {
            result = null;
        }
        try {
            result = (Travel) travelDAO.save(entity);
            travelList.add(result);
            return result;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "save");
            return null;
        }
    }

    /**
     * Metodo que modifica y guarda en la base de datos el travel, se controla
     * con SecurityUtil la contraseña.
     *
     * @param entity el travel que pasa a ser modificado
     * @return el travel modificado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public Travel modify(Travel entity) throws Exception {
        return modify(entity, true);
    }

    public Travel modify(Travel entity, boolean allowRetry) throws Exception {
        Travel result;
        boolean travellerExists = travelList.stream()
                .anyMatch(travel -> travel.getDescriptor().equalsIgnoreCase(entity.getDescriptor())
                && travel.getId() != entity.getId());
        if (travellerExists) {
            return null;
        }
        try {
            result = (Travel) travelDAO.modify(entity, entity.getId());
            rechargeList();
            for (int i = 0; i < travelList.size(); i++) {
                if (travelList.get(i).getId() == entity.getId()) {
                    travelList.set(i, result);
                }
            }
            return result;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "modify");
            return null;
        }
    }

    /**
     * Metodo que elimina un travel de la base de datos.
     *
     * @param entity el travel logeado con los datos modificados
     * @return si fue o no eliminado
     * @throws ControledException una excepción controlada
     * @throws Exception una excepción no controlada
     */
    public boolean delete(Travel entity) throws Exception {
        return delete(entity, true);
    }

    public boolean delete(Travel entity, boolean allowRetry) throws Exception {
        boolean deleted;
        try {
            deleted = travelDAO.delete(entity.getId());
            travelList.remove(entity);
            return deleted;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "delete");
            return false;
        }
    }

    public List<Travel> findAll() {
        return travelList;
    }

    public List<Travel> findByDepartment(long department) {
        return travelList.stream()
                .filter(v -> v.getDepartment() == department)
                .collect(Collectors.toList());
    }

    public Optional<Travel> findById(long travelId) {
        return travelList.stream()
                .filter(v -> v.getId() == travelId)
                .findFirst();
    }

    public static void rechargeList() throws Exception {
        rechargeList(true);
    }

    public static void rechargeList(boolean allowRetry) throws IOException, InterruptedException, Exception {
        try {
            if (LoginService.getAdminDepartment().getName().equalsIgnoreCase("Admin")) {
                travelList = travelDAO.findAll();
            } else {
                travelList = travelDAO.findByDepartment(LoginService.getAdminDepartment().getId());
            }
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
                if (allowRetry) {
                    rechargeList(false);
                } else {
                    throw new ControledException(apiE.getMessage(), "TravelService - " + method);
                }
            }
            case 401 -> {
                if (allowRetry) {
                    LoginService.relog();
                } else {
                    throw new Exception(apiE.getMessage());
                }
            }
            case 204 -> {
                throw new QuietException(apiE.getMessage(), "TravelService - " + method);
            }
            default ->
                throw new Exception(apiE.getMessage());
        }
    }
}
