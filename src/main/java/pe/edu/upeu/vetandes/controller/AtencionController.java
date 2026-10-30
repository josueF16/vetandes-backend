
package pe.edu.upeu.vetandes.controller;

import pe.edu.upeu.vetandes.dto.AtencionDTO;
import pe.edu.upeu.vetandes.dto.ReporteEspecieDTO;
import pe.edu.upeu.vetandes.service.AtencionService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AtencionController {

    private final AtencionService servicio;

    public AtencionController(AtencionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/atenciones")
    public ResponseEntity<AtencionDTO.AtencionResponse> registrar(
            @Valid @RequestBody AtencionDTO.RegistroRequest req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicio.registrar(req));
    }

    @GetMapping("/atenciones/{id}")
    public AtencionDTO.AtencionResponse obtener(
            @PathVariable Long id) {

        return servicio.obtener(id);
    }

    @GetMapping("/atenciones")
    public List<AtencionDTO.AtencionResponse> listar(
            @RequestParam(required = false) String mascota,
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta) {

        return servicio.listar(
                mascota, especie, estado, desde, hasta
        );
    }

    @PatchMapping("/atenciones/{id}/anular")
    public AtencionDTO.AtencionResponse anular(
            @PathVariable Long id,
            @Valid @RequestBody AtencionDTO.AnularRequest req) {

        return servicio.anular(id, req);
    }

    @GetMapping("/reportes/atenciones-por-especie")
    public List<ReporteEspecieDTO> reporte(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta) {

        return servicio.reporte(desde, hasta);
    }
}
