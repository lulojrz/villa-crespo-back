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

        String rol = u.getPrivilegio() != null ? u.getPrivilegio().trim() : "ROLE_PUB";
        if (!rol.startsWith("ROLE_")) {
            rol = "ROLE_" + rol;
        }

        boolean habilitado = Boolean.TRUE.equals(u.getActivo());

        return new User(
                u.getUsuario(),
                u.getContrasena(),
                habilitado,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(rol))
        );
    }
}