package com.villacrespo.back.controller;

import com.villacrespo.back.model.PlantillaTurno;
import com.villacrespo.back.model.PuntoPredicacion;
import com.villacrespo.back.model.Turno;
import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.repository.PlantillaTurnoRepository;
import com.villacrespo.back.repository.PuntoPredicacionRepository;
import com.villacrespo.back.repository.TurnoRepository;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private PlantillaTurnoRepository plantillaTurnoRepository;

    @Autowired
    private PuntoPredicacionRepository puntoPredicacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // 1. PUNTOS DE PREDICACIÓN
    // ==========================================

    @GetMapping("/puntos")
    public List<PuntoPredicacion> obtenerPuntos() {
        return puntoPredicacionRepository.findAll();
    }

    @PostMapping("/puntos")
    public PuntoPredicacion agregarPunto(@RequestBody PuntoPredicacion puntoPredicacion) {
        PuntoPredicacion guardado = puntoPredicacionRepository.save(puntoPredicacion);
        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
        return guardado;
    }

    @PutMapping("/puntos/{id}")
    public PuntoPredicacion editarPunto(@PathVariable Long id, @RequestBody PuntoPredicacion puntoPredicacion_a_editar) {
        Optional<PuntoPredicacion> puntoOpt = puntoPredicacionRepository.findById(id);

        return puntoOpt.map(punto -> {
            punto.setNombre(puntoPredicacion_a_editar.getNombre());
            punto.setDireccion(puntoPredicacion_a_editar.getDireccion());
            punto.setActivo(puntoPredicacion_a_editar.isActivo());

            PuntoPredicacion guardado = puntoPredicacionRepository.save(punto);
            messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
            return guardado;
        }).orElseThrow(() -> new RuntimeException("No se encontró el punto con ID: " + id));
    }

    // ==========================================
    // 2. PLANTILLAS DE TURNOS
    // ==========================================

    @PostMapping("/crear")
    public PlantillaTurno crearTurno(@RequestBody PlantillaTurno plantillaTurno) {
        PlantillaTurno guardada = plantillaTurnoRepository.save(plantillaTurno);
        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
        return guardada;
    }

    @GetMapping("/obtener")
    public List<PlantillaTurno> obtenerTurnos() {
        return plantillaTurnoRepository.findAll();
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> borrarTurno(@PathVariable Long id) {
        if (!plantillaTurnoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        plantillaTurnoRepository.deleteById(id);
        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
        return ResponseEntity.ok("Horario de plantilla eliminado correctamente.");
    }

    // ==========================================
    // 3. GESTIÓN DE TURNOS SEMANALES
    // ==========================================

    @GetMapping("/semana")
    public ResponseEntity<List<Turno>> obtenerTurnosSemana(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        LocalDate lunes = fecha.with(DayOfWeek.MONDAY);
        LocalDate domingo = fecha.with(DayOfWeek.SUNDAY);

        List<Turno> turnos = turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(lunes, domingo);
        return ResponseEntity.ok(turnos);
    }

    @PostMapping("/generar")
    public ResponseEntity<String> generarTurnosSemana(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lunes) {

        if (lunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            return ResponseEntity.badRequest().body("La fecha enviada debe ser un Lunes.");
        }

        List<PlantillaTurno> plantillas = plantillaTurnoRepository.findAll();
        if (plantillas.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay horarios configurados en la plantilla.");
        }

        LocalDate domingo = lunes.plusDays(6);
        List<Turno> existentes = turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(lunes, domingo);
        if (!existentes.isEmpty()) {
            return ResponseEntity.badRequest().body("Ya existen turnos generados para esta semana.");
        }

        for (PlantillaTurno plantilla : plantillas) {
            int diasSumar = plantilla.getDiaSemana().getValue() - DayOfWeek.MONDAY.getValue();
            LocalDate fechaTurno = lunes.plusDays(diasSumar);

            Turno nuevoTurno = new Turno();
            nuevoTurno.setFecha(fechaTurno);
            nuevoTurno.setHoraInicio(plantilla.getHoraInicio());
            nuevoTurno.setHoraFin(plantilla.getHoraFin());
            nuevoTurno.setPunto(plantilla.getPunto());
            nuevoTurno.setPublicador1(plantilla.getPublicador1());
            nuevoTurno.setPublicador2(plantilla.getPublicador2());

            turnoRepository.save(nuevoTurno);
        }

        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
        return ResponseEntity.ok("Se generaron con éxito los turnos para la semana del " + lunes);
    }

    @DeleteMapping("/semana")
    public ResponseEntity<String> borrarTurnosSemana(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        LocalDate lunes = fecha.with(DayOfWeek.MONDAY);
        LocalDate domingo = fecha.with(DayOfWeek.SUNDAY);

        List<Turno> turnos = turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(lunes, domingo);
        if (turnos.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay turnos generados para esa semana.");
        }

        turnoRepository.deleteByFechaBetween(lunes, domingo);
        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
        return ResponseEntity.ok("Se eliminaron " + turnos.size() + " turnos de la semana del " + lunes + ".");
    }

    // ==========================================
    // 4. INSCRIPCIÓN Y CONSULTAS POR USUARIO/AÑO
    // ==========================================

    @PutMapping("/anotarse/{id}")
    public ResponseEntity<?> anotarseEnTurno(
            @PathVariable Long id,
            @RequestParam String usuario) {

        Optional<Turno> turnoOpt = turnoRepository.findById(id);
        if (turnoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuario(usuario);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario especificado no existe.");
        }

        Turno turno = turnoOpt.get();
        Usuario user = usuarioOpt.get();

        // Desanotarse si ya está en algún slot
        if (turno.getPublicador1() != null && turno.getPublicador1().getId().equals(user.getId())) {
            turno.setPublicador1(null);
        } else if (turno.getPublicador2() != null && turno.getPublicador2().getId().equals(user.getId())) {
            turno.setPublicador2(null);
        } else if (turno.getPublicador3() != null && turno.getPublicador3().getId().equals(user.getId())) {
            turno.setPublicador3(null);
        } else if (turno.getPublicador4() != null && turno.getPublicador4().getId().equals(user.getId())) {
            turno.setPublicador4(null);
        // Anotarse en el primer slot libre
        } else if (turno.getPublicador1() == null) {
            turno.setPublicador1(user);
        } else if (turno.getPublicador2() == null) {
            turno.setPublicador2(user);
        } else if (turno.getPublicador3() == null) {
            turno.setPublicador3(user);
        } else if (turno.getPublicador4() == null) {
            turno.setPublicador4(user);
        } else {
            return ResponseEntity.badRequest().body("Este turno ya está completo (máximo 4 participantes).");
        }

        Turno actualizado = turnoRepository.save(turno);
        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/usuario/{usuario}")
    public List<Turno> encontrarTurnos(@PathVariable String usuario) {
        return turnoRepository.findByPublicador1_UsuarioOrPublicador2_Usuario(usuario, usuario);
    }

    @GetMapping("/todos")
    public List<Turno> traerTurnosPorAno(@RequestParam(required = false) String year) {
        if (year != null && !year.isEmpty()) {
            try {
                int y = Integer.parseInt(year);
                LocalDate inicio = LocalDate.of(y - 1, 9, 1);
                LocalDate fin = LocalDate.of(y, 8, 31);
                return turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(inicio, fin);
            } catch (Exception e) {
                return turnoRepository.findAll();
            }
        }
        return turnoRepository.findAll();
    }
}
