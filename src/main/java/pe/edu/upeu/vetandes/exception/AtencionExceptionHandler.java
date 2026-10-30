
package pe.edu.upeu.vetandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AtencionExceptionHandler {

    @ExceptionHandler(AtencionReglaException.class)
    public ResponseEntity<Map<String, Object>> manejar(
            AtencionReglaException ex,
            HttpServletRequest req) {

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", ex.getEstado().value());
        error.put("error", ex.getEstado().getReasonPhrase());
        error.put("mensaje", ex.getMessage());
        error.put("ruta", req.getRequestURI());

        return ResponseEntity.status(ex.getEstado()).body(error);
    }
}
