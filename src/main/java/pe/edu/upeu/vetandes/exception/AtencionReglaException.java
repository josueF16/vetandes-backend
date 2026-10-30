
package pe.edu.upeu.vetandes.exception;

import org.springframework.http.HttpStatus;

public class AtencionReglaException
        extends RuntimeException {

    private final HttpStatus estado;

    public AtencionReglaException(
            HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
