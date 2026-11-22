package cl.duocuc.pedidos.controller;

import cl.duocuc.pedidos.dto.EstadoRequest;
import cl.duocuc.pedidos.dto.PedidoRequest;
import cl.duocuc.pedidos.model.Pedido;
import cl.duocuc.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Capa de presentacion: expone los endpoints REST del microservicio. */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    // GET /api/v1/pedidos
    @GetMapping
    public List<Pedido> listar() {
        return service.listar();
    }

    // GET /api/v1/pedidos/{id}
    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // GET /api/v1/pedidos/cliente/{clienteId}
    @GetMapping("/cliente/{clienteId}")
    public List<Pedido> listarPorClienteId(@PathVariable Long clienteId) {
        return service.listarPorClienteId(clienteId);
    }

    // POST /api/v1/pedidos  -> 201 Created
    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequest request) {
        Pedido creado = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + creado.getId())).body(creado);
    }

    // PUT /api/v1/pedidos/{id}  -> cambia cliente y productos (solo si esta PENDIENTE)
    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
        return service.actualizar(id, request);
    }

    // PATCH /api/v1/pedidos/{id}/estado  -> cambia solo el estado, por ejemplo a PAGADO
    @PatchMapping("/{id}/estado")
    public Pedido cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }

    // DELETE /api/v1/pedidos/{id}  -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
