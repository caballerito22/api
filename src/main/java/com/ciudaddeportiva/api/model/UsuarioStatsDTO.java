package com.ciudaddeportiva.api.model;

public class UsuarioStatsDTO {

    private Long id;
    private String email;
    private String rol;
    private Integer reservas;   // null  →  “-” en la app

    public UsuarioStatsDTO(Long id, String email, String rol, Integer reservas) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.reservas = reservas;
    }
    public Long getId()       { return id; }
    public String getEmail()  { return email; }
    public String getRol()    { return rol; }
    public Integer getReservas() { return reservas; }
}
