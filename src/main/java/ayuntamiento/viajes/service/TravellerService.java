package ayuntamiento.viajes.service;

import ayuntamiento.viajes.dao.TravellerDAO;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.exception.ControledException;
import ayuntamiento.viajes.model.Traveller;
import java.io.IOException;

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
    }

    /**
     * Metodo que guarda un viajero en la base de datos
     *
     * @param entity el viajero que pasa a ser guardado
     * @return el viajero creado con el id
     * @throws ControledException
     * @throws Exception
     */
    // Método público con reintento por defecto
    public Traveller save(Traveller entity) throws ControledException, Exception {
        return save(entity, true);
    }

    private Traveller save(Traveller entity, boolean allowRetry) throws ControledException, Exception {
        Traveller result;
        boolean travellerExists = travellerList.stream()
                .anyMatch(traveller -> traveller.getDni().equalsIgnoreCase(entity.getDni()));
        if (travellerExists) {
            result = null;
        }
        try {
            result = (Traveller) travellerDAO.save(entity);
            travellerList.add(result);
            //rechargeList();
            return result;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "save");
            return null;
        }
    }

    /**
     * Metodo que modifica y guarda en la base de datos el viajero
     *
     * @param entity el viajero que pasa a ser modificado
     * @return el viajero modificado
     * @throws Exception
     */
    // Método público con reintento por defecto
    public Traveller modify(Traveller entity) throws Exception {
        return modify(entity, true); // permite un reintento por defecto
    }

    public Traveller modify(Traveller entity, boolean allowRetry) throws Exception {
        Traveller result;
        boolean travellerExists = travellerList.stream()
                .anyMatch(vehicle -> vehicle.getDni().equalsIgnoreCase(entity.getDni())
                && vehicle.getId() != entity.getId());
        if (travellerExists) {
            return null;
        }
        try {
            result = (Traveller) travellerDAO.modify(entity, entity.getId());
            //rechargeList();
            for (int i = 0; i < travellerList.size(); i++) {
                if (travellerList.get(i).getId() == entity.getId()) {
                    travellerList.set(i, result);
                }
            }
            return result;
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "modify");
            return null;
        }
    }

    // Método público con reintento por defecto

    /**
     * Método que borra un viajero de la base de datos
     *
     * @param entity el viajero a borrar
     * @return un booleano que confirma si se ha realizado el borrado correctamente
     * @throws Exception
     */
    public boolean delete(Traveller entity) throws Exception {
        return delete(entity, true);
    }

    public boolean delete(Traveller entity, boolean allowRetry) throws Exception {
        boolean deleted;
        try {
            deleted = travellerDAO.delete(entity.getId());
            /*if (deleted) {
                rechargeList();
            }*/
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

    public List<Traveller> findByTrip(int trip) {
        return travellerList.stream()
                .filter(v -> v.getTrip().ordinal() == trip)
                .collect(Collectors.toList());
    }

    public List<Traveller> findByOffice(int office) {
        return travellerList.stream()
                .filter(v -> v.getOffice().ordinal() == office)
                .collect(Collectors.toList());
    }

    public static void rechargeList() throws Exception {
        rechargeList(true);
    }

    public static void rechargeList(boolean allowRetry) throws IOException, InterruptedException, Exception {
        try {
            travellerList = travellerDAO.findAll();
        } catch (APIException apiE) {
            errorHandler(apiE, allowRetry, "rechargeList");
        }
        /*travellerList = List.of(
                new Traveller("12345678A", "Ana Pérez", 0, 0, "01/06/2023"),
                new Traveller("23456789B", "Luis Gómez", 1, 1, "01/06/2023"),
                new Traveller("34567890C", "María López", 2, 2, "20/07/2023"),
                new Traveller("45678901D", "Carlos Ruiz", 0, 3, "05/06/2023"),
                new Traveller("56789012E", "Laura Martín", 1, 4, "12/07/2023"),
                new Traveller("67890123F", "Javier Sánchez", 2, 5, "10/06/2023"),
                new Traveller("78901234G", "Sofía Torres", 0, 0, "20/08/2023"),
                new Traveller("89012345H", "Miguel Fernández", 1, 1, "30/09/2023"),
                new Traveller("90123456I", "Elena Díaz", 2, 2, "07/09/2023"),
                new Traveller("01234567J", "Pedro Morales", 0, 3, "19/06/2023"),
                new Traveller("11223344K", "Isabel Ruiz", 1, 4, "12/07/2023"),
                new Traveller("22334455L", "Raúl Jiménez", 2, 5, "28/06/2023")
                
        );*/

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
