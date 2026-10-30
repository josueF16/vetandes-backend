package pe.edu.upeu.vetandes.mapper;

import pe.edu.upeu.vetandes.dto.CatalogoDTO.*;
import pe.edu.upeu.vetandes.entity.*;

public final class CatalogoMapper {

    private CatalogoMapper() {
    }

    public static PrestacionResponse respuesta(Prestacion p) {
        return new PrestacionResponse(
                p.getId(), p.getCodigo(), p.getNombre(),
                p.getPrecio(), p.getEspecieAplicable(),
                Integer.valueOf(1).equals(p.getActivo())
        );
    }

    public static MascotaResponse respuesta(Mascota m) {
        return new MascotaResponse(
                m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(),
                m.getFechaNacimiento(), m.getSexo(),
                m.getPropietario().getId(),
                m.getPropietario().getNombres()
        );
    }

    public static PropietarioResponse respuesta(Propietario p) {
        return new PropietarioResponse(
                p.getId(), p.getDni(), p.getNombres(), p.getTelefono()
        );
    }

    public static void copiar(PrestacionRequest dto, Prestacion p) {
        p.setCodigo(dto.codigo());
        p.setNombre(dto.nombre().trim());
        p.setPrecio(dto.precio());
        p.setEspecieAplicable(dto.especieAplicable());
        p.setActivo(Boolean.TRUE.equals(dto.activo()) ? 1 : 0);
    }

    public static void copiar(
            MascotaRequest dto, Mascota m, Propietario propietario) {

        m.setNombre(dto.nombre().trim());
        m.setEspecie(dto.especie());
        m.setRaza(dto.raza());
        m.setFechaNacimiento(dto.fechaNacimiento());
        m.setSexo(dto.sexo());
        m.setPropietario(propietario);
    }
}