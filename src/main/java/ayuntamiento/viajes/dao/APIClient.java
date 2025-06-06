package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-05
 * @version 1.0
 */
public abstract class APIClient<T> {

    protected final String BASE_URL = PropertiesUtil.getProperty("API_URL");
    private final Class<T> typeParameterClass;
    protected final HttpClient client = HttpClient.newHttpClient();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected String endpoint;

    public APIClient(Class<T> typeParameterClass, String endpoint) {
        this.typeParameterClass = typeParameterClass;
        this.endpoint = endpoint;
    }

    public List<T> findAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint))
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, typeParameterClass));
    }

    public T findById(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/" + id))
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), typeParameterClass);
    }

    public T save(T obj) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(obj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), typeParameterClass);
    }

    public T modify(T obj, long id) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(obj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/" + id))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), typeParameterClass);
    }

    public boolean delete(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + endpoint + "/" + id))
                .header("Authorization", "Bearer " + LoginService.getSecret_token())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 || response.statusCode() == 204;
    }
}
