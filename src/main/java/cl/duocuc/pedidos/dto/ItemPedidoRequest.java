package cl.duocuc.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Una linea pedida por el usuario: que producto y cuantas unidades. */
public record ItemPedidoRequest(

        @NotNull(message = "El productoId es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer cantidad) {
}
