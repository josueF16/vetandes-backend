package pe.edu.upeu.vetandes.config;

import pe.edu.upeu.vetandes.security.*;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UsuarioDetailsService usuarios;
    private final RespuestaSeguridad errores;

    public JwtAuthenticationFilter(
            JwtService jwt,
            UsuarioDetailsService usuarios,
            RespuestaSeguridad errores) {
        this.jwt = jwt;
        this.usuarios = usuarios;
        this.errores = errores;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String ruta = req.getServletPath();

        return req.getMethod().equalsIgnoreCase("OPTIONS")
                || ruta.equals("/api/v1/auth/login")
                || ruta.startsWith("/swagger-ui")
                || ruta.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String username = jwt.validar(header.substring(7));

                UserDetails usuario =
                        usuarios.loadUserByUsername(username);

                if (!usuario.isEnabled()) {
                    throw new BadCredentialsException("Usuario inactivo");
                }

                var auth = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities()
                );

                auth.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(req)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(auth);

            } catch (JwtException | AuthenticationException |
                     IllegalArgumentException ex) {

                SecurityContextHolder.clearContext();
                errores.commence(req, res,
                        new BadCredentialsException("Token inválido", ex));
                return;
            }
        }

        chain.doFilter(req, res);
    }
}