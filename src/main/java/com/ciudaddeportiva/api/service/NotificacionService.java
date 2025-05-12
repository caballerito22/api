package com.ciudaddeportiva.api.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;


//sirve para que cuando usemos enviarNotificacion(...) se dispara inmediatamente
//una push-notification a todos tus usuarios registrados en OneSignal
@Service
public class NotificacionService {

    //cogemos el appId y la restApiKey de application.properties
    @Value("${onesignal.app_id}")
    private String appId;

    @Value("${onesignal.rest_api_key}")
    private String restApiKey;

    public void enviarNotificacion(String titulo, String mensaje, String imagenUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = "{"
                + "\"app_id\": \"" + appId + "\","
                + "\"included_segments\": [\"All\"],"
                + "\"headings\": {\"en\": \"" + titulo + "\"},"
                + "\"contents\": {\"en\": \"" + mensaje + "\"}";

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            jsonBody += ", \"big_picture\": \"" + imagenUrl + "\"";
        }

        jsonBody += "}";

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://onesignal.com/api/v1/notifications")
                .post(body)
                .addHeader("Authorization", "Basic " + restApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Notificación enviada con éxito: " + response.body().string());
            } else {
                System.out.println("Error al enviar la notificación: " + response.body().string());
            }
        }
    }
}
