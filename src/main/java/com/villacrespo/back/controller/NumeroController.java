package com.villacrespo.back.controller;

import com.villacrespo.back.model.Numero;
import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.repository.NumeroRepository;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api")
@CrossOrigin(origins = "*")
public class NumeroController {

    @Autowired
    private NumeroRepository numeroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/numeros")
    public List<Numero> traerNumeros() {
        return numeroRepository.findAll();
    }

    @PutMapping("/editar/{id}")
    public Numero editarNumero(@RequestBody Numero numero_a_editar, @PathVariable Long id) {
        return numeroRepository.findById(id).map(numeroExistente -> {
            numeroExistente.setContesta(numero_a_editar.getContesta());
            numeroExistente.setReservado(numero_a_editar.getReservado());
            numeroExistente.setUltimaFecha(numero_a_editar.getUltimaFecha());
            numeroExistente.setTocar(numero_a_editar.getTocar());
            numeroExistente.setEdificio(numero_a_editar.getEdificio());
            numeroExistente.setFechaReserva(numero_a_editar.getFechaReserva());

            // 1. Manejo de ultUsuario
            if (numero_a_editar.getUltUsuario() != null && numero_a_editar.getUltUsuario().getUsuario() != null) {
                Optional<Usuario> userCompleto = usuarioRepository.findByUsuario(numero_a_editar.getUltUsuario().getUsuario());
                userCompleto.ifPresent(numeroExistente::setUltUsuario);
            } else {
                numeroExistente.setUltUsuario(null);
            }

            // 2. Manejo de reservadoA
            if (numero_a_editar.getReservadoA() != null && numero_a_editar.getReservadoA().getUsuario() != null) {
                Optional<Usuario> userCompleto = usuarioRepository.findByUsuario(numero_a_editar.getReservadoA().getUsuario());
                userCompleto.ifPresent(numeroExistente::setReservadoA);
            } else {
                numeroExistente.setReservadoA(null);
            }

            return numeroRepository.save(numeroExistente);
        }).orElse(null);
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrarNumero(@PathVariable Long id) {
        Optional<Numero> numero = numeroRepository.findById(id);
        if (numero.isPresent()) {
            numeroRepository.deleteById(id);
            return ResponseEntity.ok("Eliminado exitosamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo encontrar el número para borrar");
        }
    }

    @PostMapping("/agregar")
    public Numero agregarNumero(@RequestBody Numero numero) {
        if (numero != null) {
            return numeroRepository.save(numero);
        }
        return null;
    }
}
