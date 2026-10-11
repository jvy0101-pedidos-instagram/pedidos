package cl.duocuc.pedidos.service;

import cl.duocuc.pedidos.exception.RecursoNoEncontradoException;
import cl.duocuc.pedidos.model.*;
import cl.duocuc.pedidos.repository.PedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Capa de negocio: el controller llama al service y el service al repository. */
@Service
public class PedidoService {

    private final PedidoRepository repository;

    // Inyeccion de dependencias por constructor.
    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
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
    public Pedido crear(Pedido nuevo) {
        if (nuevo.getEstado() == null) nuevo.setEstado(EstadoPedido.PENDIENTE);
        nuevo.setId(null); // el id lo genera la base de datos
        return repository.save(nuevo);
    }

    @Transactional
    public Pedido actualizar(Long id, Pedido datos) {
        Pedido existente = buscarPorId(id);
        existente.setClienteId(datos.getClienteId());
        existente.setProductoId(datos.getProductoId());
        existente.setCantidad(datos.getCantidad());
        existente.setTotal(datos.getTotal());
        if (datos.getEstado() != null) existente.setEstado(datos.getEstado());
        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Pedido existente = buscarPorId(id);
        repository.delete(existente);
    }
}
