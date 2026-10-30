
package pe.edu.upeu.vetandes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class AtencionDTO {

    private AtencionDTO() {}

    public record DetalleRequest(
            @NotNull(message = "La prestación es obligatoria")
            Long prestacionId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = 3, message = "La cantidad máxima es 3")
            Integer cantidad
    ) {}

    public record RegistroRequest(
            @NotNull(message = "La mascota es obligatoria")
            Long mascotaId,

            @NotNull(message = "El peso es obligatorio")
            @DecimalMin(
                    value = "0.01",
                    message = "El peso debe ser mayor que cero"
            )
            @Digits(integer = 3, fraction = 2)
            BigDecimal peso,

            @Size(max = 300)
            String diagnostico,

            @NotNull(message = "El detalle es obligatorio")
            @Size(
                    min = 1,
                    max = 8,
                    message = "La atención requiere entre 1 y 8 detalles"
            )
            List<@NotNull @Valid DetalleRequest> detalles
    ) {}

    public record AnularRequest(
            @NotBlank(message = "El motivo es obligatorio")
            @Size(
                    min = 10,
                    max = 200,
                    message = "El motivo debe tener entre 10 y 200 caracteres"
            )
            String motivo
    ) {}

    public record DetalleResponse(
            Long id,
            Long prestacionId,
            String prestacionNombre,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {}

    public record AtencionResponse(
            Long id,
            String numero,
            LocalDateTime fecha,
            Long mascotaId,
            String mascotaNombre,
            String especie,
            BigDecimal peso,
            String diagnostico,
            String estado,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total,
            String usuarioRegistro,
            String motivoAnulacion,
            List<DetalleResponse> detalles
    ) {}
}
