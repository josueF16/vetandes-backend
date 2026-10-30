package pe.edu.upeu.vetandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.vetandes.entity.Prestacion;

import java.util.List;

public interface PrestacionRepository
        extends JpaRepository<Prestacion, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Query("""
        select p from Prestacion p
        where (:especie is null
            or p.especieAplicable = :especie
            or p.especieAplicable = 'TODAS')
        order by p.codigo
        """)
    List<Prestacion> buscar(@Param("especie") String especie);

    @Query(value = """
        select count(*) from detalle_atencion
        where prestacion_id = :id
        """, nativeQuery = true)
    long contarUsos(@Param("id") Long id);
}