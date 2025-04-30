package com.ciudaddeportiva.api.model;

public class UsuarioStatsDTO {
    private String email;
    private String rol;
    private int cantidadReservas;

    public UsuarioStatsDTO(String email, String rol, int cantidadReservas) {
        this.email = email;
        this.rol = rol;
        this.cantidadReservas = cantidadReservas;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    public int getCantidadReservas() {
        return cantidadReservas;
    }
}
