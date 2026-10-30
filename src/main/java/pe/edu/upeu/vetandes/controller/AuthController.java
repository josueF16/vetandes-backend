package pe.edu.upeu.vetandes.controller;

import pe.edu.upeu.vetandes.config.JwtService;
import pe.edu.upeu.vetandes.entity.Usuario;
import pe.edu.upeu.vetandes.repository.UsuarioRepository;
import pe.edu.upeu.vetandes.security.PermisosPorRol;
import pe.edu.upeu.vetandes.exception.AtencionReglaException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UsuarioRepository usuarios,
                          PasswordEncoder encoder,
                          JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String token,
            String tipo,
            long expiraEnSegundos,
            String nombreCompleto,
            List<String> roles,
            List<String> permisos
    ) {}

    public record PerfilResponse(
            String username,
            String nombreCompleto,
            List<String> roles,
            List<String> permisos
    ) {}

    private PerfilResponse perfil(Usuario u) {
        List<String> roles = u.getRoles().stream()
                .map(r -> r.getNombre()).sorted().toList();

        List<String> permisos = PermisosPorRol.obtener(roles)
                .stream().sorted().toList();

        return new PerfilResponse(
                u.getUsername(), u.getNombreCompleto(),
                roles, permisos
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {

        Usuario u = usuarios.findByUsername(req.username())
                .orElse(null);

        if (u == null ||
                !Integer.valueOf(1).equals(u.getActivo()) ||
                !encoder.matches(req.password(), u.getPassword())) {

            throw new AtencionReglaException(
                    HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        PerfilResponse p = perfil(u);

        return new LoginResponse(
                jwt.generar(u.getUsername()),
                "Bearer",
                3600,
                p.nombreCompleto(),
                p.roles(),
                p.permisos()
        );
    }

    @GetMapping("/me")
    public PerfilResponse me(Authentication auth) {
        Usuario u = usuarios.findByUsername(auth.getName())
                .orElseThrow(() -> new AtencionReglaException(
                        HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        return perfil(u);
    }
}