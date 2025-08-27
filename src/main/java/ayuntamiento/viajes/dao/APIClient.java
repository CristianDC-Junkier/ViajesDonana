package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.exception.APIException;
import ayuntamiento.viajes.service.LoginService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Clase base de los DAO, se encargan de ofrecer los controles a las diferentes
 * clases dao, permitiendo simplemente cambiar el valor T por el valor de la
 * clase que la instanció.
 *
 *
 * @author Cristian Delgado Cruz
 * @param <T> valor de la subclase
 * @since 2025-06-05
 * @version 1.1
 */
public abstract class APIClient<T> {

    protected final String BASE_URL = PropertiesUtil.getProperty("API_URL");
    private final Class<T> typeParameterClass;
    protected final HttpClient client = HttpClient.newHttpClient();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected String endpoint;

    /**
     * Constructor único de la clase, es invocado por sus hijas
     *
     * @param typeParameterClass Tipo de la subclase
     * @param endpoint dirección final a la que se envía la petición.
     */
    public APIClient(Class<T> typeParameterClass, String endpoint) {
        this.typeParameterClass = typeParameterClass;
        this.endpoint = endpoint;
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<T> findAll() throws APIException, Exception {
        //System.out.println(BASE_URL + "/" + endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint))
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        //System.out.println(statusCode);
        switch (statusCode) {
            case 200 -> {
                //System.out.println(response.body());
                return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass));
            }
            case 204 -> {
                throw new APIException(statusCode, "Lista vacia");
            }
            default -> {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String message = jsonNode.has("error") ? jsonNode.get("error").asText() : "Error en la API ";
                throw new APIException(statusCode, message);
            }
        }
    }

    public T findById(long id) throws APIException, Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/" + id))
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200) {
            return objectMapper.readValue(response.body(), typeParameterClass);
        } else {
            throw new APIException(statusCode, responseBody);
        }
    }

    public List<T> findByDepartment(long department) throws APIException, Exception {
        //String uriStr = BASE_URL + "/" + endpoint + "/department/" + department;
        //System.out.println("Enviando request a: " + uriStr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/department/" + department))
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        switch (statusCode) {
            case 200 -> {
                return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass));
            }
            case 204 -> {
                throw new APIException(statusCode, "Lista vacia por departamento: " + department);
            }
            default -> {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String message = jsonNode.has("error") ? jsonNode.get("error").asText() : "Error en la API ";
                throw new APIException(statusCode, message);
            }
        }
    }

    public T save(T obj) throws APIException, Exception {
        String json = objectMapper.writeValueAsString(obj);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200) {
            return objectMapper.readValue(response.body(), typeParameterClass);
        } else {
            throw new APIException(statusCode, responseBody);
        }
    }

    public T modify(T obj, long id) throws APIException, Exception {
        String json = objectMapper.writeValueAsString(obj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/" + id))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200) {
            return objectMapper.readValue(response.body(), typeParameterClass);
        } else {
            throw new APIException(statusCode, responseBody);
        }
    }

    public boolean delete(long id) throws APIException, Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/" + id))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200 || response.statusCode() == 204) {
            return true;
        } else {
            throw new APIException(statusCode, responseBody);
        }
    }

}
