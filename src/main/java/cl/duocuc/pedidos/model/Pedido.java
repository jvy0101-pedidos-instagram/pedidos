package cl.duocuc.pedidos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cabecera del pedido. Un pedido tiene una o varias lineas (DetallePedido).
 * El total ya no lo envia el usuario: lo calcula el service con los precios de Catalogo.
 */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia a otro microservicio (Clientes): se guarda el id y se valida por HTTP.
    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Relacion uno a muchos: un pedido tiene muchas lineas.
     * mappedBy indica que la clave foranea (pedido_id) esta en la tabla detalle_pedido.
     * cascade ALL guarda y borra las lineas junto con el pedido.
     * orphanRemoval borra de la base una linea que se quita de la lista.
     * EAGER carga las lineas junto con el pedido para poder devolverlas en el JSON.
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Agrega una linea y deja enlazados los dos lados de la relacion. */
    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }

    /** Se ejecuta antes del INSERT: registra la fecha automaticamente. */
    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
