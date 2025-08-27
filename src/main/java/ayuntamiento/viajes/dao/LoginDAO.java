package ayuntamiento.viajes.dao;

import ayuntamiento.viajes.common.PropertiesUtil;
import ayuntamiento.viajes.exception.LoginException;
import ayuntamiento.viajes.model.Worker;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Clase que se encarga de hacer la conexión con la base de datos para el manejo
 * de los datos del login
 *
 * @author Cristian Delgado Cruz
 * @since 2025-06-02
 * @version 1.0
 */
public class LoginDAO extends APIClient<Worker> {

    public LoginDAO() {
        super(Worker.class,"status");
    }

    /**
     * Llama a la API de login y devuelve el JWT, en caso de error de conexión
     * lo reintenta un total de 3 veces más con intervalos de 2 segundos.
     *
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return JsonNode con el token y el id llamado
     * @throws LoginException Excepcion Controlada
     */
    public JsonNode login(String username, String password) throws LoginException, Exception{
        int maxRetries = 3;
        int delayMs = 2000;

        JsonNode loginPayload = objectMapper.createObjectNode()
                .put("username", username)
                .put("password", password);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/" + endpoint + "/login"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + PropertiesUtil.getProperty("API_TOKEN"))
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginPayload)))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                switch (response.statusCode()) {
                    case 200 -> {
                        return objectMapper.readTree(response.body());
                    }
                    case 401 ->
                        throw new LoginException("Credenciales inválidas", 401);
                    case 403 ->
                        throw new LoginException("Acceso prohibido o sin permisos", 403);
                    default ->
                        throw new LoginException("Error inesperado: " + response.statusCode(), response.statusCode());
                }

            } catch (UnknownHostException | ConnectException e) {
                if (attempt == maxRetries) {
                    throw new LoginException("No se pudo conectar al servidor después de varios intentos.", 503);
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    throw new LoginException("La operación fue interrumpida durante el reintento.", 500);
                }
            } catch (IOException | InterruptedException e) {
                throw new LoginException("Error al intentar comunicarse con el servidor: " + e.getMessage(), 503);
            }
        }

        throw new LoginException("Fallo desconocido", 500);
    }

}
