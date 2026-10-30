package pe.edu.upeu.vetandes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.CatalogoDTO.*;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.NegocioException;
import pe.edu.upeu.vetandes.mapper.CatalogoMapper;
import pe.edu.upeu.vetandes.repository.*;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoService {

    private final PrestacionRepository prestaciones;
    private final MascotaRepository mascotas;
    private final PropietarioRepository propietarios;

    public List<PropietarioResponse> propietarios() {
        return propietarios.findAll().stream()
                .map(CatalogoMapper::respuesta).toList();
    }

    public List<PrestacionResponse> prestaciones(String especie) {
        if (especie != null &&
                !Set.of("PERRO", "GATO", "OTRO", "TODAS").contains(especie)) {
            throw new NegocioException(400, "Filtro de especie inválido.");
        }

        return prestaciones.buscar(especie).stream()
                .map(CatalogoMapper::respuesta).toList();
    }

    public PrestacionResponse prestacion(Long id) {
        return CatalogoMapper.respuesta(buscarPrestacion(id));
    }

    @Transactional
    public PrestacionResponse crearPrestacion(PrestacionRequest dto) {
        if (prestaciones.existsByCodigo(dto.codigo())) {
            throw new NegocioException(409,
                    "Ya existe una prestación con ese código.");
        }

        Prestacion p = new Prestacion();
        CatalogoMapper.copiar(dto, p);
        p = prestaciones.saveAndFlush(p);

        log.info("Prestación creada: id={}, código={}", p.getId(), p.getCodigo());
        return CatalogoMapper.respuesta(p);
    }

    @Transactional
    public PrestacionResponse editarPrestacion(Long id, PrestacionRequest dto) {
        Prestacion p = buscarPrestacion(id);

        if (prestaciones.existsByCodigoAndIdNot(dto.codigo(), id)) {
            throw new NegocioException(409,
                    "Ya existe otra prestación con ese código.");
        }

        CatalogoMapper.copiar(dto, p);
        prestaciones.flush();

        log.info("Prestación actualizada: id={}", id);
        return CatalogoMapper.respuesta(p);
    }

    @Transactional
    public PrestacionResponse cambiarEstado(Long id, EstadoRequest dto) {
        Prestacion p = buscarPrestacion(id);
        p.setActivo(Boolean.TRUE.equals(dto.activo()) ? 1 : 0);

        log.info("Estado de prestación actualizado: id={}, activo={}",
                id, dto.activo());

        return CatalogoMapper.respuesta(p);
    }

    @Transactional
    public void eliminarPrestacion(Long id) {
        Prestacion p = buscarPrestacion(id);

        if (prestaciones.contarUsos(id) > 0) {
            throw new NegocioException(409,
                    "La prestación tiene atenciones. Debes darla de baja.");
        }

        prestaciones.delete(p);
        prestaciones.flush();
        log.info("Prestación eliminada: id={}", id);
    }

    public List<MascotaResponse> mascotas(Long propietarioId) {
        if (propietarioId != null) {
            buscarPropietario(propietarioId);
        }

        return mascotas.buscar(propietarioId).stream()
                .map(CatalogoMapper::respuesta).toList();
    }

    public MascotaResponse mascota(Long id) {
        return CatalogoMapper.respuesta(buscarMascota(id));
    }

    @Transactional
    public MascotaResponse crearMascota(MascotaRequest dto) {
        Propietario propietario = buscarPropietario(dto.propietarioId());

        if (mascotas.existsByNombreAndPropietario_Id(
                dto.nombre().trim(), dto.propietarioId())) {
            throw new NegocioException(409,
                    "El propietario ya tiene una mascota con ese nombre.");
        }

        Mascota m = new Mascota();
        CatalogoMapper.copiar(dto, m, propietario);
        m = mascotas.saveAndFlush(m);

        log.info("Mascota creada: id={}, propietario={}",
                m.getId(), propietario.getId());

        return CatalogoMapper.respuesta(m);
    }

    @Transactional
    public MascotaResponse editarMascota(Long id, MascotaRequest dto) {
        Mascota m = buscarMascota(id);
        Propietario propietario = buscarPropietario(dto.propietarioId());

        if (mascotas.existsByNombreAndPropietario_IdAndIdNot(
                dto.nombre().trim(), dto.propietarioId(), id)) {
            throw new NegocioException(409,
                    "El propietario ya tiene otra mascota con ese nombre.");
        }

        CatalogoMapper.copiar(dto, m, propietario);
        mascotas.flush();

        log.info("Mascota actualizada: id={}", id);
        return CatalogoMapper.respuesta(m);
    }

    @Transactional
    public void eliminarMascota(Long id) {
        Mascota m = buscarMascota(id);

        if (mascotas.contarAtenciones(id) > 0) {
            throw new NegocioException(409,
                    "No se puede eliminar una mascota con atenciones.");
        }

        mascotas.delete(m);
        mascotas.flush();
        log.info("Mascota eliminada: id={}", id);
    }

    private Prestacion buscarPrestacion(Long id) {
        return prestaciones.findById(id).orElseThrow(() ->
                new NegocioException(404, "Prestación no encontrada."));
    }

    private Mascota buscarMascota(Long id) {
        return mascotas.findById(id).orElseThrow(() ->
                new NegocioException(404, "Mascota no encontrada."));
    }

    private Propietario buscarPropietario(Long id) {
        return propietarios.findById(id).orElseThrow(() ->
                new NegocioException(404, "Propietario no encontrado."));
    }
}