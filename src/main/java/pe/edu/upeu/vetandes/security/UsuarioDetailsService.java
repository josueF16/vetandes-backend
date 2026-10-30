
package pe.edu.upeu.vetandes.security;

import pe.edu.upeu.vetandes.entity.Usuario;
import pe.edu.upeu.vetandes.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repositorio;

    public UsuarioDetailsService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = repositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"
                ));

        var roles = usuario.getRoles()
                .stream()
                .map(r -> r.getNombre())
                .toList();

        var permisos = PermisosPorRol.obtener(roles);

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!Integer.valueOf(1).equals(usuario.getActivo()))
                .authorities(permisos.toArray(String[]::new))
                .build();
    }
}
