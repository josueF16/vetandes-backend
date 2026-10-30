package pe.edu.upeu.vetandes.security;

import jakarta.servlet.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RespuestaSeguridad implements
        AuthenticationEntryPoint, AccessDeniedHandler {

    private void responder(HttpServletRequest req,
                           HttpServletResponse res,
                           int codigo, String error,
                           String mensaje) throws IOException {

        res.setStatus(codigo);
        res.setContentType("application/json;charset=UTF-8");

        String ruta = req.getRequestURI()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        res.getWriter().write(String.format("""
            {
              "timestamp": "%s",
              "status": %d,
              "error": "%s",
              "mensaje": "%s",
              "ruta": "%s"
            }
            """, LocalDateTime.now(), codigo, error, mensaje, ruta));
    }

    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse res,
                         AuthenticationException ex) throws IOException {
        responder(req, res, 401, "Unauthorized",
                "Token ausente, inválido o vencido");
    }

    @Override
    public void handle(HttpServletRequest req,
                       HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        responder(req, res, 403, "Forbidden",
                "No tiene permisos para realizar esta operación");
    }
}