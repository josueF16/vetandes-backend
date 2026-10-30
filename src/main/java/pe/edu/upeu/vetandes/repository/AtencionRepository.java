
package pe.edu.upeu.vetandes.repository;

import pe.edu.upeu.vetandes.entity.Atencion;
import pe.edu.upeu.vetandes.dto.ReporteEspecieDTO;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AtencionRepository
        extends JpaRepository<Atencion, Long>,
        JpaSpecificationExecutor<Atencion> {

    @Query(value = """
        SELECT NVL(MAX(TO_NUMBER(SUBSTR(numero, 4))), 0)
        FROM atencion
        """, nativeQuery = true)
    Long ultimoNumero();

    @Query("""
        SELECT new pe.edu.upeu.vetandes.dto.ReporteEspecieDTO(
            a.mascota.especie, COUNT(a.id), SUM(a.total))
        FROM Atencion a
        WHERE a.estado <> 'ANULADA'
        GROUP BY a.mascota.especie
        ORDER BY SUM(a.total) DESC
        """)
    List<ReporteEspecieDTO> reporteGeneral();

    @Query("""
        SELECT new pe.edu.upeu.vetandes.dto.ReporteEspecieDTO(
            a.mascota.especie, COUNT(a.id), SUM(a.total))
        FROM Atencion a
        WHERE a.estado <> 'ANULADA'
        AND a.fecha >= :desde
        GROUP BY a.mascota.especie
        ORDER BY SUM(a.total) DESC
        """)
    List<ReporteEspecieDTO> reporteDesde(
            @Param("desde") LocalDateTime desde
    );

    @Query("""
        SELECT new pe.edu.upeu.vetandes.dto.ReporteEspecieDTO(
            a.mascota.especie, COUNT(a.id), SUM(a.total))
        FROM Atencion a
        WHERE a.estado <> 'ANULADA'
        AND a.fecha < :hasta
        GROUP BY a.mascota.especie
        ORDER BY SUM(a.total) DESC
        """)
    List<ReporteEspecieDTO> reporteHasta(
            @Param("hasta") LocalDateTime hasta
    );

    @Query("""
        SELECT new pe.edu.upeu.vetandes.dto.ReporteEspecieDTO(
            a.mascota.especie, COUNT(a.id), SUM(a.total))
        FROM Atencion a
        WHERE a.estado <> 'ANULADA'
        AND a.fecha >= :desde
        AND a.fecha < :hasta
        GROUP BY a.mascota.especie
        ORDER BY SUM(a.total) DESC
        """)
    List<ReporteEspecieDTO> reporteRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
