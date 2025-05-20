package com.ciudaddeportiva.api.model;

public class CambiarEstadoRequest {

    private Long    partidoId;     // ← mismo nombre que usamos en el JSON
    private String nuevoEstado;

    public CambiarEstadoRequest() {}              // constructor vacío  ✔️
    public CambiarEstadoRequest(Long id, String estado) {
        this.partidoId   = id;
        this.nuevoEstado = estado;
    }

    /* getters — Spring los usa para leer */

    public Long getPartidoId() {
        return partidoId;
    }

    public String getNuevoEstado() { return nuevoEstado; }

    /* setters — Jackson los necesita para des-serializar */

    public void setPartidoId(Long partidoId) {
        this.partidoId = partidoId;
    }

    public void setNuevoEstado(String nuevoEstado){ this.nuevoEstado = nuevoEstado; }
}
