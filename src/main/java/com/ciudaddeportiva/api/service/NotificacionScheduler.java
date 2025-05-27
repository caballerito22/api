package com.ciudaddeportiva.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//sirve para recordatorios automáticos

@Service
public class NotificacionScheduler {

    @Autowired
    private NotificacionService notificacionService;

    @Scheduled(cron = "0 0 * * * *")  //para que se haga cada hora
    public void enviarRecordatorios() {
        try {
            String titulo = "Recordatorio de Partido";
            String mensaje = "¡No olvides tu partido de hoy!";
            notificacionService.enviarNotificacion(titulo, mensaje, null);
            System.out.println("Recordatorio enviado: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

