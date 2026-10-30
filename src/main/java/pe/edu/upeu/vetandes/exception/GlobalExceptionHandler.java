package pe.edu.upeu.vetandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> responder(
            int status, String mensaje, HttpServletRequest request,
            Map<String, String> campos) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        body.put("mensaje", mensaje);
        body.put("ruta", request.getRequestURI());

        if (campos != null) {
            body.put("campos", campos);
        }

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, Object>> negocio(
            NegocioException ex, HttpServletRequest request) {

        return responder(ex.getStatus(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> campos = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                campos.putIfAbsent(
                        error.getField(), error.getDefaultMessage()));

        return responder(400, "Datos inválidos", request, campos);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> formato(
            Exception ex, HttpServletRequest request) {

        return responder(400,
                "Formato de datos inválido. Revisa los tipos y las fechas.",
                request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("Conflicto de integridad en {}", request.getRequestURI());

        return responder(409,
                "La operación genera un duplicado o afecta datos relacionados.",
                request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> prohibido(
            AccessDeniedException ex, HttpServletRequest request) {

        return responder(403,
                "No tienes permiso para realizar esta operación.",
                request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(
            Exception ex, HttpServletRequest request) {

        log.error("Error inesperado en {}", request.getRequestURI(), ex);

        return responder(500,
                "Ocurrió un error interno al procesar la solicitud.",
                request, null);
    }
}