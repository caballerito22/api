package com.ciudaddeportiva.api.model;

//se encarga de cambiar el estado de un partido / entrene

public class CambiarEstadoRequest {

    private Long    partidoId;
    private String nuevoEstado;

    public CambiarEstadoRequest() {}
    public CambiarEstadoRequest(Long id, String estado) {
        this.partidoId   = id;
        this.nuevoEstado = estado;
    }


    public Long getPartidoId() {
        return partidoId;
    }

    public String getNuevoEstado() { return nuevoEstado; }

    public void setPartidoId(Long partidoId) {
        this.partidoId = partidoId;
    }
    public void setNuevoEstado(String nuevoEstado){ this.nuevoEstado = nuevoEstado; }
}
