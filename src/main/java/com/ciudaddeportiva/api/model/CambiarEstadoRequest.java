package com.ciudaddeportiva.api.model;

public class CambiarEstadoRequest {

    private int    partidoId;     // ← mismo nombre que usamos en el JSON
    private String nuevoEstado;

    public CambiarEstadoRequest() {}              // constructor vacío  ✔️
    public CambiarEstadoRequest(int id, String estado) {
        this.partidoId   = id;
        this.nuevoEstado = estado;
    }

    /* getters — Spring los usa para leer */
    public int    getPartidoId()   { return partidoId;   }
    public String getNuevoEstado() { return nuevoEstado; }

    /* setters — Jackson los necesita para des-serializar */
    public void setPartidoId(int partidoId)       { this.partidoId = partidoId; }
    public void setNuevoEstado(String nuevoEstado){ this.nuevoEstado = nuevoEstado; }
}
