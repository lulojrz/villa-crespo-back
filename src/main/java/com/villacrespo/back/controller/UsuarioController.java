package com.villacrespo.back.controller;

import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.model.cambiarContrasenaDTO;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Usuario> traerUsuarios() {
        return usuarioRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ANC', 'SM')")
    @PutMapping("editar/{id}")
    public ResponseEntity<?> editarUsuario(@RequestBody Usuario usuario_a_editar, @PathVariable Long id) {
        return usuarioRepository.findById(id).map(usuario -> {
            if (usuario_a_editar.getNombre() != null) {
                usuario.setNombre(usuario_a_editar.getNombre());
            }
            if (usuario_a_editar.getApellido() != null) {
                usuario.setApellido(usuario_a_editar.getApellido());
            }
            if (usuario_a_editar.getPrivilegio() != null) {
                usuario.setPrivilegio(usuario_a_editar.getPrivilegio());
            }
            if (usuario_a_editar.getAsignacion() != null) {
                usuario.setAsignacion(usuario_a_editar.getAsignacion());
            }
            if (usuario_a_editar.getNumeroTelefono() != null) {
                usuario.setNumeroTelefono(usuario_a_editar.getNumeroTelefono());
            }
            if (usuario_a_editar.getAptoPredicacionPublica() != null) {
                usuario.setAptoPredicacionPublica(usuario_a_editar.getAptoPredicacionPublica());
            }
            if (usuario_a_editar.getTieneCarrito() != null) {
                usuario.setTieneCarrito(usuario_a_editar.getTieneCarrito());
            }
            if (usuario_a_editar.getTieneBanner() != null) {
                usuario.setTieneBanner(usuario_a_editar.getTieneBanner());
            }
            if (usuario_a_editar.getGrupoServicio() != null) {
                usuario.setGrupoServicio(usuario_a_editar.getGrupoServicio());
            }
            if (usuario_a_editar.getActivo() != null) {
                usuario.setActivo(usuario_a_editar.getActivo());
            }
            if (usuario_a_editar.getContrasena() != null && !usuario_a_editar.getContrasena().trim().isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(usuario_a_editar.getContrasena().trim()));
            }

            Usuario guardado = usuarioRepository.save(usuario);
            return ResponseEntity.ok(guardado);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PreAuthorize("hasAnyRole('ANC', 'SM')")
    @PostMapping("/crear")
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario_nuevo) {
        if (usuario_nuevo == null || usuario_nuevo.getUsuario() == null || usuario_nuevo.getUsuario().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de usuario es requerido.");
        }

        usuario_nuevo.setUsuario(usuario_nuevo.getUsuario().trim());

        if (usuario_nuevo.getPrivilegio() == null || usuario_nuevo.getPrivilegio().trim().isEmpty()) {
            usuario_nuevo.setPrivilegio("ROLE_PUB");
        }
        if (usuario_nuevo.getAsignacion() == null || usuario_nuevo.getAsignacion().trim().isEmpty()) {
            usuario_nuevo.setAsignacion("NINGUNA");
        }
        if (usuario_nuevo.getActivo() == null) {
            usuario_nuevo.setActivo(true);
        }
        if (usuario_nuevo.getGrupoServicio() == null) {
            usuario_nuevo.setGrupoServicio(0);
        }
        if (usuario_nuevo.getAptoPredicacionPublica() == null) {
            usuario_nuevo.setAptoPredicacionPublica(false);
        }
        if (usuario_nuevo.getTieneCarrito() == null) {
            usuario_nuevo.setTieneCarrito(false);
        }
        if (usuario_nuevo.getTieneBanner() == null) {
            usuario_nuevo.setTieneBanner(false);
        }

        String rawPassword = usuario_nuevo.getContrasena();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "prueba123";
        }
        usuario_nuevo.setContrasena(passwordEncoder.encode(rawPassword.trim()));

        Usuario guardado = usuarioRepository.save(usuario_nuevo);
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("cambiar-contrasena")
    public ResponseEntity<String> cambiarContrasena(
            @RequestBody cambiarContrasenaDTO datos,
            Authentication authentication) {

        if (datos == null || datos.getContrasenaActual() == null || datos.getNuevaContrasena() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Datos incompletos");
        }

        String usernameToken = authentication.getName();
        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsuarioIgnoreCase(usernameToken);
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOptional.get();

        boolean match = passwordEncoder.matches(datos.getContrasenaActual(), usuario.getContrasena());
        if (!match) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Contraseña actual incorrecta");
        }

        usuario.setContrasena(passwordEncoder.encode(datos.getNuevaContrasena().trim()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("correcto");
    }

    @PreAuthorize("hasAnyRole('ANC', 'SM')")
    @DeleteMapping("borrar/{id}")
    public ResponseEntity<?> borrarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioEliminado = usuarioRepository.findById(id);
        if (usuarioEliminado.isPresent()) {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok(usuarioEliminado.get());
        }
        return ResponseEntity.notFound().build();
    }
}