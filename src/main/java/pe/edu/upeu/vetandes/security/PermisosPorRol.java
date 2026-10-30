
package pe.edu.upeu.vetandes.security;

import java.util.*;

public final class PermisosPorRol {

    private PermisosPorRol() {}

    private static final Set<String> TODOS = Set.of(
            "PRESTACION_LEER",
            "PRESTACION_ESCRIBIR",
            "MASCOTA_LEER",
            "MASCOTA_ESCRIBIR",
            "ATENCION_LEER",
            "ATENCION_REGISTRAR",
            "ATENCION_ANULAR",
            "REPORTE_VER"
    );

    private static final Map<String, Set<String>> MATRIZ = Map.of(
            "ADMIN", TODOS,

            "RECEPCIONISTA", Set.of(
                    "PRESTACION_LEER",
                    "MASCOTA_LEER",
                    "MASCOTA_ESCRIBIR",
                    "ATENCION_LEER",
                    "ATENCION_REGISTRAR"
            ),

            "VETERINARIO", Set.of(
                    "PRESTACION_LEER",
                    "PRESTACION_ESCRIBIR",
                    "MASCOTA_LEER",
                    "ATENCION_LEER",
                    "ATENCION_ANULAR",
                    "REPORTE_VER"
            )
    );

    public static Set<String> obtener(Collection<String> roles) {

        Set<String> permisos = new HashSet<>();

        for (String rol : roles) {
            permisos.addAll(
                    MATRIZ.getOrDefault(rol, Set.of())
            );
        }

        return permisos;
    }
}
