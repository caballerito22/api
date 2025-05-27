package com.ciudaddeportiva.api.model;

//DTO - estadísticas por usuario: email, rol y reservas que ha hecho (jugador -)

public class UsuarioStatsDTO {

    private Long id;
    private String email;
    private String rol;
    private Integer reservas;

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
