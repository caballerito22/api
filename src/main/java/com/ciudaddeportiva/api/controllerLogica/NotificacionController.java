package com.ciudaddeportiva.api.controllerLogica;


import com.ciudaddeportiva.api.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

//controlador para enviar notificaciones manualmente
//es un Endpoint REST para enviar notificaciones
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<String> enviarNotificacion(
            @RequestParam String titulo,
            @RequestParam String mensaje,
            @RequestParam(required = false) String imagenUrl) {
        try {
            notificacionService.enviarNotificacion(titulo, mensaje, imagenUrl);
            return ResponseEntity.ok("Notificación enviada con éxito");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar la notificación: " + e.getMessage());
        }
    }
}
