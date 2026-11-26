package com.villacrespo.back.controller;

import com.villacrespo.back.model.PlantillaTurno;
import com.villacrespo.back.model.PuntoPredicacion;
import com.villacrespo.back.model.Turno;
import com.villacrespo.back.model.Usuario;
import com.villacrespo.back.repository.PlantillaTurnoRepository;
import com.villacrespo.back.repository.PuntoPredicacionRepository;
import com.villacrespo.back.repository.TurnoRepository;
import com.villacrespo.back.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoRepository turnoRepository;
    private final PlantillaTurnoRepository plantillaTurnoRepository;
    private final PuntoPredicacionRepository puntoPredicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public TurnoController(
            TurnoRepository turnoRepository,
            PlantillaTurnoRepository plantillaTurnoRepository,
            PuntoPredicacionRepository puntoPredicacionRepository,
            UsuarioRepository usuarioRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.turnoRepository = turnoRepository;
        this.plantillaTurnoRepository = plantillaTurnoRepository;
        this.puntoPredicacionRepository = puntoPredicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ==========================================
    // 1. PUNTOS DE PREDICACIÓN
    // ==========================================

    @GetMapping("/puntos")
    public List<PuntoPredicacion> obtenerPuntos() {
        return puntoPredicacionRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ANCIANO', 'SIERVO_MINISTERIAL')")
    @PostMapping("/puntos")
    public PuntoPredicacion agregarPunto(@RequestBody PuntoPredicacion puntoPredicacion) {
        PuntoPredicacion guardado = puntoPredicacionRepository.save(puntoPredicacion);
        notificarCambio();
        return guardado;
    }

    @PreAuthorize("hasAnyRole('ANCIANO', 'SIERVO_MINISTERIAL')")
    @PutMapping("/puntos/{id}")
    public ResponseEntity<PuntoPredicacion> editarPunto(
            @PathVariable Long id,
            @RequestBody PuntoPredicacion datos) {
        return puntoPredicacionRepository.findById(id).map(punto -> {
            punto.setNombre(datos.getNombre());
            punto.setDireccion(datos.getDireccion());
            punto.setActivo(datos.isActivo());

            PuntoPredicacion guardado = puntoPredicacionRepository.save(punto);
            notificarCambio();
            return ResponseEntity.ok(guardado);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ==========================================
    // 2. PLANTILLAS DE TURNOS
    // ==========================================

    @PreAuthorize("hasAnyRole('ANCIANO', 'SIERVO_MINISTERIAL')")
    @PostMapping("/crear")
    public PlantillaTurno crearTurno(@RequestBody PlantillaTurno plantillaTurno) {
        PlantillaTurno guardada = plantillaTurnoRepository.save(plantillaTurno);
        notificarCambio();
        return guardada;
    }

    @GetMapping("/obtener")
    public List<PlantillaTurno> obtenerTurnos() {
        return plantillaTurnoRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ANCIANO', 'SIERVO_MINISTERIAL')")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> borrarTurno(@PathVariable Long id) {
        if (!plantillaTurnoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        plantillaTurnoRepository.deleteById(id);
        notificarCambio();
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

    @PreAuthorize("hasAnyRole('ANCIANO', 'SIERVO_MINISTERIAL')")
    @Transactional
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

        List<Turno> nuevosTurnos = new ArrayList<>(plantillas.size());
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
            nuevosTurnos.add(nuevoTurno);
        }

        // Inserción en lote en lugar de N queries individuales
        turnoRepository.saveAll(nuevosTurnos);
        notificarCambio();
        return ResponseEntity.ok("Se generaron con éxito los turnos para la semana del " + lunes);
    }

    @PreAuthorize("hasAnyRole('ANCIANO', 'SIERVO_MINISTERIAL')")
    @Transactional
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
        notificarCambio();
        return ResponseEntity.ok("Se eliminaron " + turnos.size() + " turnos de la semana del " + lunes + ".");
    }

    // ==========================================
    // 4. INSCRIPCIÓN Y CONSULTAS ESCALABLES
    // ==========================================

    @Transactional
    @PutMapping("/anotarse/{id}")
    public ResponseEntity<?> anotarseEnTurno(
            @PathVariable Long id,
            @RequestParam(required = false) String usuario,
            Authentication authentication) {

        Optional<Turno> turnoOpt = turnoRepository.findById(id);
        if (turnoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String usuarioAutenticado = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANCIANO") || a.getAuthority().equals("ROLE_SIERVO_MINISTERIAL"));

        // Un publicador común solo puede anotarse/desanotarse a sí mismo;
        // un Anciano/SM puede anotar a otro usuario si envía el parámetro
        String objetivo = (usuario != null && !usuario.isBlank() && esAdmin)
                ? usuario.trim()
                : usuarioAutenticado;

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsuarioIgnoreCase(objetivo);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario especificado no existe.");
        }

        Turno turno = turnoOpt.get();
        Usuario user = usuarioOpt.get();

        if (turno.getPublicador1() != null && turno.getPublicador1().getId().equals(user.getId())) {
            turno.setPublicador1(null);
        } else if (turno.getPublicador2() != null && turno.getPublicador2().getId().equals(user.getId())) {
            turno.setPublicador2(null);
        } else if (turno.getPublicador3() != null && turno.getPublicador3().getId().equals(user.getId())) {
            turno.setPublicador3(null);
        } else if (turno.getPublicador4() != null && turno.getPublicador4().getId().equals(user.getId())) {
            turno.setPublicador4(null);
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
        notificarCambio();
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<Turno>> encontrarTurnos(
            @PathVariable String usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {

        LocalDate fechaDesde = (desde != null) ? desde : LocalDate.now().minusMonths(1);
        return ResponseEntity.ok(turnoRepository.findTurnosDeUsuarioDesde(usuario.trim(), fechaDesde));
    }

    // Siempre acota por año de servicio (sep-ago) para no traer miles de registros históricos de golpe
    @GetMapping("/todos")
    public ResponseEntity<List<Turno>> traerTurnosPorAno(@RequestParam(required = false) Integer year) {
        int anioServicio = (year != null) ? year : calcularAnioServicioActual();
        LocalDate inicio = LocalDate.of(anioServicio - 1, 9, 1);
        LocalDate fin = LocalDate.of(anioServicio, 8, 31);
        return ResponseEntity.ok(turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(inicio, fin));
    }

    // Endpoint paginado para reportes o tablas grandes en el frontend (?page=0&size=50)
    @GetMapping("/paginado")
    public ResponseEntity<Page<Turno>> traerTurnosPaginados(
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 50, sort = {"fecha", "horaInicio"}, direction = Sort.Direction.ASC) Pageable pageable) {

        int anioServicio = (year != null) ? year : calcularAnioServicioActual();
        LocalDate inicio = LocalDate.of(anioServicio - 1, 9, 1);
        LocalDate fin = LocalDate.of(anioServicio, 8, 31);
        return ResponseEntity.ok(turnoRepository.findByFechaBetween(inicio, fin, pageable));
    }

    private int calcularAnioServicioActual() {
        LocalDate hoy = LocalDate.now();
        return (hoy.getMonthValue() >= 9) ? hoy.getYear() + 1 : hoy.getYear();
    }

    private void notificarCambio() {
        messagingTemplate.convertAndSend("/topic/turnos", "Actualizacion");
    }
}