package cl.duocuc.pedidos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Una linea del pedido: que producto, cuantas unidades y a que precio se vendio. */
@Entity
@Table(name = "detalle_pedido")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacion muchos a uno: muchas lineas pertenecen a un pedido.
     * JoinColumn crea la columna pedido_id, que es la clave foranea hacia la tabla pedidos.
     * JsonIgnore evita un ciclo infinito al convertir a JSON (pedido -> detalle -> pedido...).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;

    // Referencia a otro microservicio (Catalogo): se guarda el id y se valida por HTTP.
    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidad;

    // Precio copiado desde Catalogo al momento de la compra: si despues cambia, el pedido no cambia.
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
