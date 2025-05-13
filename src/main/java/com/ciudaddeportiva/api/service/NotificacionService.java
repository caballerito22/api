package com.ciudaddeportiva.api.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

// Servicio para enviar notificaciones push a OneSignal
@Service
public class NotificacionService {

    @Value("${onesignal.app_id}")
    private String appId;

    @Value("${onesignal.rest_api_key}")
    private String restApiKey;

    public void enviarNotificacion(String titulo, String mensaje, String imagenUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Construye el cuerpo del mensaje JSON
        String jsonBody = "{"
                + "\"app_id\": \"" + appId + "\","
                + "\"included_segments\": [\"All\"],"
                + "\"headings\": {\"en\": \"" + titulo + "\"},"
                + "\"contents\": {\"en\": \"" + mensaje + "\"}";

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            jsonBody += ", \"big_picture\": \"" + imagenUrl + "\"";
        }

        jsonBody += "}";

        // Construye la solicitud HTTP
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://onesignal.com/api/v1/notifications")
                .post(body)
                .addHeader("Authorization", "Bearer " + restApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        // Envía la solicitud
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                System.out.println("=== DEBUG === Notificación enviada con éxito: " + responseBody);
            } else {
                System.out.println("=== ERROR  === Falló el envío de la notificación: " + responseBody);
                throw new RuntimeException("Error en OneSignal: " + responseBody);
            }
        } catch (Exception e) {
            System.out.println("=== ERROR  === Excepción al enviar notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}