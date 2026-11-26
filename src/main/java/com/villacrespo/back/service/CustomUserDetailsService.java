package com.villacrespo.back.service;

import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;

    public CustomUserDetailsService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepo.findByUsuarioIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        String rol = normalizarRol(u.getPrivilegio());
        boolean habilitado = Boolean.TRUE.equals(u.getActivo());

        return new User(
                u.getUsuario(),
                u.getContrasena(),
                habilitado,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
        );
    }

    private String normalizarRol(String privilegio) {
        if (privilegio == null || privilegio.isBlank()) {
            return "PUBLICADOR";
        }
        String limpio = privilegio.trim().toUpperCase();
        if (limpio.startsWith("ROLE_")) {
            limpio = limpio.substring(5);
        }
        return switch (limpio) {
            case "ANC", "ANCIANO" -> "ANCIANO";
            case "SM", "SIERVO_MINISTERIAL" -> "SIERVO_MINISTERIAL";
            case "PR", "PRECURSOR_REGULAR" -> "PRECURSOR_REGULAR";
            default -> "PUBLICADOR";
        };
    }
}