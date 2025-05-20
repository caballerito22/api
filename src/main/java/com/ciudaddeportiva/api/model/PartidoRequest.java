package com.ciudaddeportiva.api.model;

public class PartidoRequest {

    private String fecha;          // formato "2025-05-05"
    private String hora;           // formato "18:00"
    private String campo;
    private String equipoLocal;
    private String equipoVisitante;
    private String tipoReserva;   // "partido" o "entrenamiento"
    private Long usuarioId;        // ID del usuario creador

    // Getters y setters

    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }
    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getCampo() {
        return campo;
    }
    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }
    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public String getEquipoVisitante() {
        return equipoVisitante;
    }
    public void setEquipoVisitante(String equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }

    public String getTipoReserva() {
        return tipoReserva;
    }
    public void setTipoReserva(String tipoReserva) {
        this.tipoReserva = tipoReserva;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
