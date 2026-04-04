package io.enrow.catchallverifier;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Lightweight wrapper around the Enrow catch-all email verifier API.
 *
 * <p>All methods are static -- no instantiation needed. Every call returns a
 * {@code Map<String, Object>} parsed from the JSON response.</p>
 */
public final class CatchAllVerifier {

    private static final String BASE_URL = "https://api.enrow.io";
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private CatchAllVerifier() {}

    /**
     * Start a single catch-all email verification.
     *
     * <p>Required key in {@code params}: {@code "email"}.
     * Optional keys: {@code "settings"}.</p>
     *
     * @param apiKey Enrow API key
     * @param params request body fields
     * @return parsed JSON response containing at least {@code "id"}
     */
    public static Map<String, Object> verify(String apiKey, Map<String, Object> params) {
        return post(apiKey, "/email/verify/single", params);
    }

    /**
     * Retrieve the result of a single email verification.
     *
     * @param apiKey Enrow API key
     * @param id     verification ID returned by {@link #verify}
     * @return parsed JSON response
     */
    public static Map<String, Object> get(String apiKey, String id) {
        return get(apiKey, "/email/verify/single?id=" + id);
    }

    /**
     * Start a bulk catch-all email verification (up to 5,000 per batch).
     *
     * <p>Required key in {@code params}: {@code "verifications"} (a list of maps, each
     * containing at least {@code "email"}).
     * Optional keys: {@code "settings"}.</p>
     *
     * @param apiKey Enrow API key
     * @param params request body fields
     * @return parsed JSON response containing at least {@code "batchId"}
     */
    public static Map<String, Object> verifyBulk(String apiKey, Map<String, Object> params) {
        return post(apiKey, "/email/verify/bulk", params);
    }

    /**
     * Retrieve the results of a bulk email verification.
     *
     * @param apiKey Enrow API key
     * @param id     batch ID returned by {@link #verifyBulk}
     * @return parsed JSON response
     */
    public static Map<String, Object> getBulk(String apiKey, String id) {
        return get(apiKey, "/email/verify/bulk?id=" + id);
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private static Map<String, Object> post(String apiKey, String path, Map<String, Object> body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();
        return send(request);
    }

    private static Map<String, Object> get(String apiKey, String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("x-api-key", apiKey)
                .GET()
                .build();
        return send(request);
    }

    private static Map<String, Object> send(HttpRequest request) {
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> data = GSON.fromJson(response.body(), MAP_TYPE);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String message = data != null && data.containsKey("message")
                        ? String.valueOf(data.get("message"))
                        : "API error " + response.statusCode();
                throw new RuntimeException(message);
            }
            return data;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Request failed: " + e.getMessage(), e);
        }
    }
}
