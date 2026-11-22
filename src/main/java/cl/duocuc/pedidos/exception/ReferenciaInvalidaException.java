package cl.duocuc.pedidos.exception;

/** El pedido apunta a un cliente o producto que no existe en su microservicio. */
public class ReferenciaInvalidaException extends RuntimeException {
    public ReferenciaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
