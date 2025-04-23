package com.ciudaddeportiva.api.model;

public class CambiarEstadoRequest {
    private int    idPartido;
    private String nuevoEstado;

    public CambiarEstadoRequest() {}                 // ← obligatorio para Jackson

    public CambiarEstadoRequest(int id, String estado){
        this.idPartido   = id;
        this.nuevoEstado = estado;
    }

    /**  Spring necesita *getters* con estos nombres  */
    public int    getIdPartido() { return idPartido; }
    public String getNuevoEstado() { return nuevoEstado; }
}
