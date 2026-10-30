package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class CatalogoDTO {

    private CatalogoDTO() {
    }

    public record PrestacionRequest(
            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "PRS-\\d{3}",
                    message = "El código debe tener el formato PRS-000")
            String codigo,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 3, max = 80,
                    message = "El nombre debe tener entre 3 y 80 caracteres")
            String nombre,

            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio debe ser mayor que cero")
            @Digits(integer = 8, fraction = 2,
                    message = "El precio admite hasta 8 enteros y 2 decimales")
            BigDecimal precio,

            @NotBlank(message = "La especie es obligatoria")
            @Pattern(regexp = "PERRO|GATO|TODAS",
                    message = "La especie debe ser PERRO, GATO o TODAS")
            String especieAplicable,

            @NotNull(message = "El estado es obligatorio")
            Boolean activo
    ) {
    }

    public record PrestacionResponse(
            Long id,
            String codigo,
            String nombre,
            BigDecimal precio,
            String especieAplicable,
            boolean activo
    ) {
    }

    public record EstadoRequest(
            @NotNull(message = "El estado es obligatorio")
            Boolean activo
    ) {
    }

    public record MascotaRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 40,
                    message = "El nombre debe tener entre 2 y 40 caracteres")
            String nombre,

            @NotBlank(message = "La especie es obligatoria")
            @Pattern(regexp = "PERRO|GATO|OTRO",
                    message = "La especie debe ser PERRO, GATO u OTRO")
            String especie,

            @Size(max = 40,
                    message = "La raza admite hasta 40 caracteres")
            String raza,

            @NotNull(message = "La fecha de nacimiento es obligatoria")
            @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
            LocalDate fechaNacimiento,

            @NotBlank(message = "El sexo es obligatorio")
            @Pattern(regexp = "M|H", message = "El sexo debe ser M o H")
            String sexo,

            @NotNull(message = "El propietario es obligatorio")
            @Positive(message = "El identificador debe ser positivo")
            Long propietarioId
    ) {
    }

    public record MascotaResponse(
            Long id,
            String nombre,
            String especie,
            String raza,
            LocalDate fechaNacimiento,
            String sexo,
            Long propietarioId,
            String propietarioNombre
    ) {
    }

    public record PropietarioResponse(
            Long id,
            String dni,
            String nombres,
            String telefono
    ) {
    }
}