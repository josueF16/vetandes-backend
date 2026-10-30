
package pe.edu.upeu.vetandes.dto;

import java.math.BigDecimal;

public record ReporteEspecieDTO(
        String especie,
        Long cantidadAtenciones,
        BigDecimal importeTotal
) {}
