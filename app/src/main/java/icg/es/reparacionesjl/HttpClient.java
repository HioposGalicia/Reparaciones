package icg.es.reparacionesjl;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {

    private static final String TAG = "ReparacionesJL";
    private static final int TIMEOUT_MS = 10000;

    private static final boolean USE_MOCK = false;

    private static final String MOCK_JSON = "{"
            + "\"status\":\"ok\","
            + "\"code\":\"REPARACION_ENCONTRADA\","
            + "\"message\":\"Mock\","
            + "\"data\":{"
            + "\"reparacion\":{"
            + "\"id\":32,"
            + "\"ticket_reparacion\":\"02VS6UN796A\","
            + "\"importe\":\"15.26\","
            + "\"en_garantia\":false,"
            + "\"trabajos\":["
            + "{"
            + "\"id\":74,"
            + "\"descripcion\":\"Achicar 2\","
            + "\"unidades\":2,"
            + "\"importe\":\"7.26\","
            + "\"coste\":\"2.00\","
            + "\"metal\":\"1.0000\","
            + "\"trabajo_tipo\":{"
            + "\"descripcion\":\"Achicar\","
            + "\"codigos_x_pais\":["
            + "{\"id_pais\":144,\"codigo\":\"21214\",\"descripcion\":\"Achicar\"}"
            + "]"
            + "}"
            + "},"
            + "{"
            + "\"id\":75,"
            + "\"descripcion\":\"Pulir y limpiar\","
            + "\"unidades\":1,"
            + "\"importe\":\"8.00\","
            + "\"coste\":\"3.00\","
            + "\"metal\":\"0.0000\","
            + "\"trabajo_tipo\":{"
            + "\"descripcion\":\"Pulir y limpiar\","
            + "\"codigos_x_pais\":[]"
            + "}"
            + "}"
            + "],"
            + "\"estado\":{"
            + "\"id\":9,"
            + "\"identificador\":\"RECIBIDA_EN_TIENDA\","
            + "\"nombre\":\"Recibida en tienda\""
            + "}"
            + "}"
            + "}"
            + "}";

    public static ReparacionResponse getReparacion(String codigoTicket) {
        if (USE_MOCK) {
            Log.d(TAG, "GET reparacion - MODO MOCK para: " + codigoTicket);
            Gson gson = new Gson();
            return gson.fromJson(MOCK_JSON, ReparacionResponse.class);
        }

        String apiUrl = SessionManager.getInstance().getApiBaseUrl()
                + "/api/v1/reparacion/ticket/" + codigoTicket;
        String apiKey = SessionManager.getInstance().getApiKey();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "GET reparacion - ResponseCode: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                Log.d(TAG, "GET reparacion - JSON: " + response.toString());
                Gson gson = new Gson();
                return gson.fromJson(response.toString(), ReparacionResponse.class);

            } else {
                // Leer el cuerpo del error desde ErrorStream
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.e(TAG, "GET reparacion - Error HTTP " + responseCode + ": " + response.toString());

                Gson gson = new Gson();
                return gson.fromJson(response.toString(), ReparacionResponse.class);
            }

        } catch (Exception e) {
            Log.e(TAG, "GET reparacion - Exception: " + e.getMessage());
            return null;

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static boolean patchReparacionEntregada(String codigoTicket) {
        if (USE_MOCK) {
            Log.d(TAG, "PATCH entregada - MODO MOCK para: " + codigoTicket);
            return true;
        }

        String apiUrl = SessionManager.getInstance().getApiBaseUrl()
                + "/api/v1/reparacion/" + codigoTicket + "/entregada";
        String apiKey = SessionManager.getInstance().getApiKey();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "PATCH entregada - ResponseCode: " + responseCode);

            return responseCode == HttpURLConnection.HTTP_OK
                    || responseCode == HttpURLConnection.HTTP_NO_CONTENT;

        } catch (Exception e) {
            Log.e(TAG, "PATCH entregada - Exception: " + e.getMessage());
            return false;

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public static boolean patchReparacionRevertir(String codigoTicket) {
        if (USE_MOCK) {
            Log.d(TAG, "PATCH revertir - MODO MOCK para: " + codigoTicket);
            return true;
        }

        String apiUrl = SessionManager.getInstance().getApiBaseUrl()
                + "/api/v1/reparacion/" + codigoTicket + "/pendiente";
        String apiKey = SessionManager.getInstance().getApiKey();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "PATCH revertir - ResponseCode: " + responseCode);

            return responseCode == HttpURLConnection.HTTP_OK
                    || responseCode == HttpURLConnection.HTTP_NO_CONTENT;

        } catch (Exception e) {
            Log.e(TAG, "PATCH revertir - Exception: " + e.getMessage());
            return false;

        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}