package cl.duocuc.pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Lo que el usuario envia para crear o modificar un pedido.
 * No trae total ni precios: esos datos los pone el sistema.
 */
public record PedidoRequest(

        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        // @Valid hace que tambien se validen las reglas de cada item de la lista.
        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Valid
        List<ItemPedidoRequest> items) {
}
