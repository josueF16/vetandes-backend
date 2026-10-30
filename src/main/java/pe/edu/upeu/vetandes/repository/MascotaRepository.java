package pe.edu.upeu.vetandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.vetandes.entity.Mascota;

import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    boolean existsByNombreAndPropietario_Id(
            String nombre, Long propietarioId);

    boolean existsByNombreAndPropietario_IdAndIdNot(
            String nombre, Long propietarioId, Long id);

    @Query("""
        select m from Mascota m
        join fetch m.propietario p
        where (:propietarioId is null or p.id = :propietarioId)
        order by m.nombre
        """)
    List<Mascota> buscar(@Param("propietarioId") Long propietarioId);

    @Query(value = """
        select count(*) from atencion where mascota_id = :id
        """, nativeQuery = true)
    long contarAtenciones(@Param("id") Long id);
}