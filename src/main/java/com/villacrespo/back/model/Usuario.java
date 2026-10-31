package com.villacrespo.back.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String usuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "grupo_servicio")
    private Integer grupoServicio;

    @Column(name = "numero_telefono", length = 20)
    private String numeroTelefono;

    @Column(nullable = false, length = 50)
    private String privilegio = "ROLE_PUB";

    @Column(nullable = false, length = 50)
    private String asignacion = "NINGUNA";

    @Column(name = "apto_predicacion_publica", nullable = false)
    private Boolean aptoPredicacionPublica = false;

    @Column(name = "tiene_carrito", nullable = false)
    private Boolean tieneCarrito = false;

    @Column(name = "tiene_banner", nullable = false)
    private Boolean tieneBanner = false;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // --- Métodos de compatibilidad con villa_crespo_front ---

    @JsonProperty("telefono")
    public String getTelefono() {
        return this.numeroTelefono;
    }

    @JsonProperty("telefono")
    public void setTelefono(String telefono) {
        this.numeroTelefono = telefono;
    }

    @JsonProperty("habilitacionPublica")
    public Boolean getHabilitacionPublica() {
        return this.aptoPredicacionPublica != null ? this.aptoPredicacionPublica : false;
    }

    @JsonProperty("habilitacionPublica")
    public void setHabilitacionPublica(Boolean habilitacionPublica) {
        this.aptoPredicacionPublica = habilitacionPublica != null ? habilitacionPublica : false;
    }

    @JsonProperty("carrito")
    public Boolean getCarrito() {
        return this.tieneCarrito != null ? this.tieneCarrito : false;
    }

    @JsonProperty("carrito")
    public void setCarrito(Boolean carrito) {
        this.tieneCarrito = carrito != null ? carrito : false;
    }

    @JsonProperty("banner")
    public Boolean getBanner() {
        return this.tieneBanner != null ? this.tieneBanner : false;
    }

    @JsonProperty("banner")
    public void setBanner(Boolean banner) {
        this.tieneBanner = banner != null ? banner : false;
    }

    // --- CORRECCIÓN DE GRUPO ---

    @JsonProperty("grupo")
    public Object getGrupo() {
        return this.grupoServicio != null ? this.grupoServicio : "";
    }

    @JsonProperty("grupo")
    public void setGrupo(Object grupo) {
        if (grupo != null) {
            try {
                String strGrupo = grupo.toString().replaceAll("[^0-9]", "").trim();
                if (!strGrupo.isEmpty()) {
                    this.grupoServicio = Integer.parseInt(strGrupo);
                }
            } catch (Exception ignored) {
            }
        }
    }
}