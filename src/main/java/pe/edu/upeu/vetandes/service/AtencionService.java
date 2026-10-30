
package pe.edu.upeu.vetandes.service;

import pe.edu.upeu.vetandes.dto.AtencionDTO;
import pe.edu.upeu.vetandes.dto.ReporteEspecieDTO;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.AtencionReglaException;
import pe.edu.upeu.vetandes.repository.AtencionRepository;
import pe.edu.upeu.vetandes.repository.MascotaRepository;
import pe.edu.upeu.vetandes.repository.PrestacionRepository;

import jakarta.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AtencionService {

    private static final Logger log =
            LoggerFactory.getLogger(AtencionService.class);

    private final AtencionRepository atenciones;
    private final MascotaRepository mascotas;
    private final PrestacionRepository prestaciones;

    public AtencionService(
            AtencionRepository atenciones,
            MascotaRepository mascotas,
            PrestacionRepository prestaciones) {

        this.atenciones = atenciones;
        this.mascotas = mascotas;
        this.prestaciones = prestaciones;
    }

    private AtencionReglaException error(
            HttpStatus estado, String mensaje) {

        log.warn("Atención rechazada: {}", mensaje);
        return new AtencionReglaException(estado, mensaje);
    }

    private BigDecimal moneda(BigDecimal numero) {
        return numero.setScale(2, RoundingMode.HALF_UP);
    }

    // REGISTRAR ATENCIÓN CON TODOS SUS DETALLES
    @Transactional
    public synchronized AtencionDTO.AtencionResponse registrar(
            AtencionDTO.RegistroRequest req) {

        if (req == null || req.detalles() == null ||
                req.detalles().isEmpty() ||
                req.detalles().size() > 8) {

            throw error(HttpStatus.BAD_REQUEST,
                    "RN-03: Se necesitan entre 1 y 8 detalles");
        }

        if (req.peso() == null ||
                req.peso().compareTo(BigDecimal.ZERO) <= 0) {

            throw error(HttpStatus.BAD_REQUEST,
                    "RN-03: El peso debe ser positivo");
        }

        Mascota mascota = mascotas.findById(req.mascotaId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        Set<Long> repetidas = new HashSet<>();
        List<DetalleAtencion> detalles = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (AtencionDTO.DetalleRequest item : req.detalles()) {

            if (item == null ||
                    item.prestacionId() == null ||
                    item.cantidad() == null ||
                    item.cantidad() < 1 ||
                    item.cantidad() > 3) {

                throw error(HttpStatus.BAD_REQUEST,
                        "RN-03: Prestación y cantidad (1 a 3) obligatorias");
            }

            if (!repetidas.add(item.prestacionId())) {
                throw error(HttpStatus.BAD_REQUEST,
                        "RN-03: No se puede repetir una prestación");
            }

            Prestacion p = prestaciones.findById(item.prestacionId())
                    .orElseThrow(() -> error(
                            HttpStatus.NOT_FOUND,
                            "Prestación inexistente: " + item.prestacionId()
                    ));

            if (!Integer.valueOf(1).equals(p.getActivo())) {
                throw error(HttpStatus.CONFLICT,
                        "RN-03: La prestación " +
                                p.getCodigo() + " está inactiva");
            }

            if (!"TODAS".equals(p.getEspecieAplicable()) &&
                    !p.getEspecieAplicable().equals(
                            mascota.getEspecie())) {

                throw error(HttpStatus.CONFLICT,
                        "RN-03: La prestación " + p.getCodigo() +
                                " no aplica a " + mascota.getEspecie());
            }

            DetalleAtencion detalle = new DetalleAtencion();

            detalle.setPrestacion(p);
            detalle.setCantidad(item.cantidad());

            detalle.setPrecioUnitario(
                    moneda(p.getPrecio())
            );

            detalle.setSubtotal(
                    moneda(
                            p.getPrecio().multiply(
                                    BigDecimal.valueOf(item.cantidad())
                            )
                    )
            );

            subtotal = subtotal.add(detalle.getSubtotal());
            detalles.add(detalle);
        }

        subtotal = moneda(subtotal);

        // RN-04: DESCUENTO DE 10% SI HAY 3 O MÁS
        // PRESTACIONES DIFERENTES

        BigDecimal descuento = detalles.size() >= 3
                ? moneda(subtotal.multiply(new BigDecimal("0.10")))
                : moneda(BigDecimal.ZERO);

        Atencion atencion = new Atencion();

        atencion.setNumero(
                "AT-" + String.format(
                        "%06d", atenciones.ultimoNumero() + 1
                )
        );

        atencion.setFecha(LocalDateTime.now());
        atencion.setMascota(mascota);
        atencion.setPeso(req.peso());
        atencion.setDiagnostico(req.diagnostico());
        atencion.setEstado("REGISTRADA");

        atencion.setSubtotal(subtotal);
        atencion.setDescuento(descuento);
        atencion.setTotal(
                moneda(subtotal.subtract(descuento))
        );

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String usuario = auth == null ||
                !auth.isAuthenticated() ||
                "anonymousUser".equals(auth.getName())
                ? "sistema"
                : auth.getName();

        atencion.setUsuarioRegistro(usuario);

        for (DetalleAtencion detalle : detalles) {
            detalle.setAtencion(atencion);
            atencion.getDetalles().add(detalle);
        }

        Atencion guardada =
                atenciones.saveAndFlush(atencion);

        log.info(
                "Atención registrada {} con {} detalles. Total {}",
                guardada.getNumero(),
                detalles.size(),
                guardada.getTotal()
        );

        return convertir(guardada);
    }

    // CONSULTAR UNA ATENCIÓN POR ID
    @Transactional(readOnly = true)
    public AtencionDTO.AtencionResponse obtener(Long id) {

        Atencion atencion = atenciones.findById(id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Atención no encontrada"
                ));

        return convertir(atencion);
    }

    // ANULAR UNA ATENCIÓN
    @Transactional
    public AtencionDTO.AtencionResponse anular(
            Long id, AtencionDTO.AnularRequest req) {

        if (req == null ||
                req.motivo() == null ||
                req.motivo().trim().length() < 10 ||
                req.motivo().length() > 200) {

            throw error(HttpStatus.BAD_REQUEST,
                    "RN-05: El motivo debe tener entre 10 y 200 caracteres");
        }

        Atencion atencion = atenciones.findById(id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Atención no encontrada"
                ));

        if (!"REGISTRADA".equals(atencion.getEstado())) {
            throw error(HttpStatus.CONFLICT,
                    "RN-05: Solo se anulan atenciones registradas");
        }

        if (atencion.getFecha().isBefore(
                LocalDateTime.now().minusHours(48))) {

            throw error(HttpStatus.CONFLICT,
                    "RN-05: Superó las 48 horas para anulación");
        }

        atencion.setEstado("ANULADA");
        atencion.setMotivoAnulacion(req.motivo().trim());

        log.info("Atención anulada: {}", atencion.getNumero());

        return convertir(atenciones.save(atencion));
    }

    // CONSULTA CON FILTROS COMBINABLES
    @Transactional(readOnly = true)
    public List<AtencionDTO.AtencionResponse> listar(
            String nombreMascota,
            String especie,
            String estado,
            LocalDate desde,
            LocalDate hasta) {

        if (desde != null && hasta != null &&
                desde.isAfter(hasta)) {

            throw error(HttpStatus.BAD_REQUEST,
                    "El inicio no puede ser posterior al fin");
        }

        Specification<Atencion> spec = (root, query, cb) -> {

            List<Predicate> filtros = new ArrayList<>();

            if (nombreMascota != null &&
                    !nombreMascota.isBlank()) {

                filtros.add(cb.like(
                        cb.lower(root.get("mascota").get("nombre")),
                        "%" + nombreMascota.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (especie != null && !especie.isBlank()) {
                filtros.add(cb.equal(
                        root.get("mascota").get("especie"),
                        especie.toUpperCase(Locale.ROOT)
                ));
            }

            if (estado != null && !estado.isBlank()) {
                filtros.add(cb.equal(
                        root.get("estado"),
                        estado.toUpperCase(Locale.ROOT)
                ));
            }

            if (desde != null) {
                filtros.add(cb.greaterThanOrEqualTo(
                        root.get("fecha"),
                        desde.atStartOfDay()
                ));
            }

            if (hasta != null) {
                filtros.add(cb.lessThan(
                        root.get("fecha"),
                        hasta.plusDays(1).atStartOfDay()
                ));
            }

            return cb.and(filtros.toArray(new Predicate[0]));
        };

        return atenciones.findAll(
                        spec,
                        Sort.by(Sort.Direction.DESC, "fecha")
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    // REPORTE AGRUPADO POR ESPECIE
    @Transactional(readOnly = true)
    public List<ReporteEspecieDTO> reporte(
            LocalDate desde, LocalDate hasta) {

        if (desde != null && hasta != null &&
                desde.isAfter(hasta)) {

            throw error(HttpStatus.BAD_REQUEST,
                    "El inicio no puede ser posterior al fin");
        }

        if (desde != null && hasta != null) {
            return atenciones.reporteRango(
                    desde.atStartOfDay(),
                    hasta.plusDays(1).atStartOfDay()
            );
        }

        if (desde != null) {
            return atenciones.reporteDesde(
                    desde.atStartOfDay()
            );
        }

        if (hasta != null) {
            return atenciones.reporteHasta(
                    hasta.plusDays(1).atStartOfDay()
            );
        }

        return atenciones.reporteGeneral();
    }

    // CONVERTIR ENTIDAD A DTO
    private AtencionDTO.AtencionResponse convertir(Atencion a) {

        List<AtencionDTO.DetalleResponse> datos =
                a.getDetalles()
                        .stream()
                        .map(d -> new AtencionDTO.DetalleResponse(
                                d.getId(),
                                d.getPrestacion().getId(),
                                d.getPrestacion().getNombre(),
                                d.getCantidad(),
                                d.getPrecioUnitario(),
                                d.getSubtotal()
                        ))
                        .toList();

        return new AtencionDTO.AtencionResponse(
                a.getId(),
                a.getNumero(),
                a.getFecha(),
                a.getMascota().getId(),
                a.getMascota().getNombre(),
                a.getMascota().getEspecie(),
                a.getPeso(),
                a.getDiagnostico(),
                a.getEstado(),
                a.getSubtotal(),
                a.getDescuento(),
                a.getTotal(),
                a.getUsuarioRegistro(),
                a.getMotivoAnulacion(),
                datos
        );
    }
}
