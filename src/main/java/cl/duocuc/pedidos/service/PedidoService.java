package cl.duocuc.pedidos.service;

import cl.duocuc.pedidos.client.CatalogoClient;
import cl.duocuc.pedidos.client.ClienteClient;
import cl.duocuc.pedidos.dto.ItemPedidoRequest;
import cl.duocuc.pedidos.dto.PedidoRequest;
import cl.duocuc.pedidos.dto.ProductoResponse;
import cl.duocuc.pedidos.exception.RecursoNoEncontradoException;
import cl.duocuc.pedidos.exception.ReglaNegocioException;
import cl.duocuc.pedidos.model.DetallePedido;
import cl.duocuc.pedidos.model.EstadoPedido;
import cl.duocuc.pedidos.model.Pedido;
import cl.duocuc.pedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: el controller llama al service y el service al repository. */
@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final ClienteClient clienteClient;
    private final CatalogoClient catalogoClient;

    // Inyeccion de dependencias por constructor.
    public PedidoService(PedidoRepository repository, ClienteClient clienteClient,
                         CatalogoClient catalogoClient) {
        this.repository = repository;
        this.clienteClient = clienteClient;
        this.catalogoClient = catalogoClient;
    }

    public List<Pedido> listar() {
        return repository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe pedido con id " + id));
    }

    public List<Pedido> listarPorClienteId(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    @Transactional
    public Pedido crear(PedidoRequest request) {
        // 1. El cliente tiene que existir en el microservicio Clientes.
        clienteClient.verificarExiste(request.clienteId());

        Pedido pedido = new Pedido();
        pedido.setClienteId(request.clienteId());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // 2. Cada producto tiene que existir en Catalogo; el total se calcula aqui.
        armarDetalles(pedido, request.items());
        return repository.save(pedido);
    }

    @Transactional
    public Pedido actualizar(Long id, PedidoRequest request) {
        Pedido existente = buscarPorId(id);
        // Un pedido ya pagado o despachado no puede cambiar sus productos.
        if (existente.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se puede modificar un pedido PENDIENTE; este esta " + existente.getEstado());
        }
        clienteClient.verificarExiste(request.clienteId());
        existente.setClienteId(request.clienteId());

        // Se quitan las lineas antiguas (orphanRemoval las borra de la base) y se arman de nuevo.
        existente.getDetalles().clear();
        armarDetalles(existente, request.items());
        return repository.save(existente);
    }

    @Transactional
    public Pedido cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido existente = buscarPorId(id);
        existente.setEstado(nuevoEstado);
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Pedido existente = buscarPorId(id);
        repository.delete(existente); // cascade ALL borra tambien sus lineas
    }

    /**
     * Consulta cada producto en Catalogo, valida que se pueda vender,
     * crea la linea con el precio real y suma el total del pedido.
     */
    private void armarDetalles(Pedido pedido, List<ItemPedidoRequest> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest item : items) {
            ProductoResponse producto = catalogoClient.buscarProducto(item.productoId());

            if (!Boolean.TRUE.equals(producto.activo())) {
                throw new ReglaNegocioException("El producto " + producto.nombre() + " no esta activo");
            }
            if (producto.stock() == null || producto.stock() < item.cantidad()) {
                throw new ReglaNegocioException("Stock insuficiente para " + producto.nombre()
                        + ": se pidieron " + item.cantidad() + " y hay " + producto.stock());
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setProductoId(producto.id());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(producto.precio());
            detalle.setSubtotal(producto.precio().multiply(BigDecimal.valueOf(item.cantidad())));
            pedido.agregarDetalle(detalle);

            total = total.add(detalle.getSubtotal());
        }

        pedido.setTotal(total);
    }
}
