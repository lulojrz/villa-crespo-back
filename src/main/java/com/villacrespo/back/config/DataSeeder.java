package com.villacrespo.back.config;

import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Si no existe ningún usuario, crear el usuario inicial admin (ROLE_ANC)
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsuario("admin");
            admin.setContrasena(passwordEncoder.encode("admin123"));
            admin.setNombre("Admin");
            admin.setApellido("Villa Crespo");
            admin.setGrupoServicio(1);
            admin.setNumeroTelefono("1100000000");
            admin.setPrivilegio("ROLE_ANC");
            admin.setAsignacion("Soporte");
            admin.setAptoPredicacionPublica(true);
            admin.setTieneCarrito(true);
            admin.setTieneBanner(true);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println(">>> [DataSeeder] Usuario administrador creado exitosamente: usuario 'admin' / contraseña 'admin123' (ROLE_ANC)");
        }

        // 2. Garantizar que cuentas con asignación 'soporte' tengan privilegios de ANC
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            if (u.getAsignacion() != null && u.getAsignacion().toLowerCase().contains("soporte")) {
                if (!"ROLE_ANC".equalsIgnoreCase(u.getPrivilegio())) {
                    u.setPrivilegio("ROLE_ANC");
                    usuarioRepository.save(u);
                }
            }
        }
    }
}
