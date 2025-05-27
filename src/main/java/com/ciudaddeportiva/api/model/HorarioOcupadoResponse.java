package com.ciudaddeportiva.api.model;

//modelo usado para las horas ocupadas de un capo, con el día de la reserva marcado

public class HorarioOcupadoResponse {
    private String horaInicio;
    private String horaFin;

    public HorarioOcupadoResponse(String horaInicio, String horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }
}
