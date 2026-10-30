package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.CatalogoDTO.*;
import pe.edu.upeu.vetandes.service.CatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService service;

    @GetMapping("/propietarios")
    @PreAuthorize("hasAuthority('MASCOTA_LEER')")
    public List<PropietarioResponse> propietarios() {
        return service.propietarios();
    }

    @GetMapping("/prestaciones")
    @PreAuthorize("hasAuthority('PRESTACION_LEER')")
    public List<PrestacionResponse> prestaciones(
            @RequestParam(required = false) String especie) {
        return service.prestaciones(especie);
    }

    @GetMapping("/prestaciones/{id}")
    @PreAuthorize("hasAuthority('PRESTACION_LEER')")
    public PrestacionResponse prestacion(@PathVariable Long id) {
        return service.prestacion(id);
    }

    @PostMapping("/prestaciones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
    public PrestacionResponse crearPrestacion(
            @Valid @RequestBody PrestacionRequest dto) {
        return service.crearPrestacion(dto);
    }

    @PutMapping("/prestaciones/{id}")
    @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
    public PrestacionResponse editarPrestacion(
            @PathVariable Long id,
            @Valid @RequestBody PrestacionRequest dto) {
        return service.editarPrestacion(id, dto);
    }

    @PatchMapping("/prestaciones/{id}/estado")
    @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
    public PrestacionResponse estado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest dto) {
        return service.cambiarEstado(id, dto);
    }

    @DeleteMapping("/prestaciones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
    public void eliminarPrestacion(@PathVariable Long id) {
        service.eliminarPrestacion(id);
    }

    @GetMapping("/mascotas")
    @PreAuthorize("hasAuthority('MASCOTA_LEER')")
    public List<MascotaResponse> mascotas(
            @RequestParam(required = false) Long propietarioId) {
        return service.mascotas(propietarioId);
    }

    @GetMapping("/mascotas/{id}")
    @PreAuthorize("hasAuthority('MASCOTA_LEER')")
    public MascotaResponse mascota(@PathVariable Long id) {
        return service.mascota(id);
    }

    @PostMapping("/mascotas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MASCOTA_ESCRIBIR')")
    public MascotaResponse crearMascota(
            @Valid @RequestBody MascotaRequest dto) {
        return service.crearMascota(dto);
    }

    @PutMapping("/mascotas/{id}")
    @PreAuthorize("hasAuthority('MASCOTA_ESCRIBIR')")
    public MascotaResponse editarMascota(
            @PathVariable Long id,
            @Valid @RequestBody MascotaRequest dto) {
        return service.editarMascota(id, dto);
    }

    @DeleteMapping("/mascotas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MASCOTA_ESCRIBIR')")
    public void eliminarMascota(@PathVariable Long id) {
        service.eliminarMascota(id);
    }
}