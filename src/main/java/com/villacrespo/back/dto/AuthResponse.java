package com.villacrespo.back.dto;

public record AuthResponse(
        String token,
        String type,
        Long id,
        String usuario,
        String nombre,
        String apellido,
        String privilegio,
        String asignacion,
        Integer grupo,
        Boolean habilitacionPublica
) {
    public AuthResponse(
            String token,
            Long id,
            String usuario,
            String nombre,
            String apellido,
            String privilegio,
            String asignacion,
            Integer grupo,
            Boolean habilitacionPublica) {
        this(token, "Bearer", id, usuario, nombre, apellido, privilegio, asignacion, grupo, habilitacionPublica);
    }
}