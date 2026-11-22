package cl.duocuc.pedidos.exception;

/** Otro microservicio no respondio, por lo que no se puede validar el pedido. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
