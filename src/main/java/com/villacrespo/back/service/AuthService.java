package com.villacrespo.back.service;

import com.villacrespo.back.Security.JwtService;
import com.villacrespo.back.dto.AuthResponse;
import com.villacrespo.back.dto.LoginRequest;
import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepo;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioRepository usuarioRepo) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepo = usuarioRepo;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.contrasena())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        Usuario u = usuarioRepo.findByUsuarioIgnoreCase(request.usuario())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new AuthResponse(
                token,
                u.getId(),
                u.getUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getPrivilegio(),
                u.getAsignacion(),
                u.getGrupoServicio(),
                u.getHabilitacionPublica()
        );
    }
}