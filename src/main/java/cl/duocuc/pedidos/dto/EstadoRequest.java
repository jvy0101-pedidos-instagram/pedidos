package cl.duocuc.pedidos.dto;

import cl.duocuc.pedidos.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

/** Cuerpo para cambiar solo el estado del pedido, por ejemplo a PAGADO. */
public record EstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado) {
}
