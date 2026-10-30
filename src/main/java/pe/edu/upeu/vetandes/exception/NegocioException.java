package pe.edu.upeu.vetandes.exception;

public class NegocioException extends RuntimeException {

    private final int status;

    public NegocioException(int status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}