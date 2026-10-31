package com.villacrespo.back.model;

import lombok.Data;

@Data
public class cambiarContrasenaDTO {
    private String usuario;
    private String contrasenaActual;
    private String nuevaContrasena;
}
