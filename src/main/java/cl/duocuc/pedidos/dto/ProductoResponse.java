package cl.duocuc.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Los datos de un producto que Pedidos necesita, tal como los responde Catalogo.
 * ignoreUnknown descarta los campos que Pedidos no usa (sku, talla, color...).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoResponse(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer stock,
        Boolean activo) {
}
