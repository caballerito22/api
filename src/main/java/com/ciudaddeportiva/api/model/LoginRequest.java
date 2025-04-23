package com.ciudaddeportiva.api.model;

public class LoginRequest {
    private String email;
    private String password;
    private String rol;

    // Constructor vacío (requerido por Spring)
    public LoginRequest() {}

    // Constructor con email y password (login)
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor completo (registro)
    public LoginRequest(String email, String password, String rol) {
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
