package cl.duocuc.pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duocuc.pedidos.client.CatalogoClient;
import cl.duocuc.pedidos.client.ClienteClient;
import cl.duocuc.pedidos.dto.ItemPedidoRequest;
import cl.duocuc.pedidos.dto.PedidoRequest;
import cl.duocuc.pedidos.dto.ProductoResponse;
import cl.duocuc.pedidos.exception.ReferenciaInvalidaException;
import cl.duocuc.pedidos.exception.ReglaNegocioException;
import cl.duocuc.pedidos.model.Pedido;
import cl.duocuc.pedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias de las reglas de negocio de Pedidos.
 * Clientes, Catalogo y la base de datos se reemplazan por "mocks" (objetos simulados),
 * asi la prueba no necesita que los otros microservicios ni MySQL esten corriendo.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository repository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private PedidoService service;

    @Test
    void crearCalculaElTotalConElPrecioDeCatalogo() {
        // Catalogo responde que el producto 1 cuesta 10.000 y tiene 5 unidades.
        when(catalogoClient.buscarProducto(1L))
                .thenReturn(new ProductoResponse(1L, "Polera", new BigDecimal("10000"), 5, true));
        // save devuelve el mismo pedido que recibe.
        when(repository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido creado = service.crear(new PedidoRequest(1L, List.of(new ItemPedidoRequest(1L, 2))));

        assertThat(creado.getTotal()).isEqualByComparingTo("20000");
        assertThat(creado.getDetalles()).hasSize(1);
        assertThat(creado.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo("10000");
    }

    @Test
    void crearRechazaUnClienteQueNoExiste() {
        doThrow(new ReferenciaInvalidaException("No existe cliente con id 999"))
                .when(clienteClient).verificarExiste(999L);

        assertThatThrownBy(() ->
                service.crear(new PedidoRequest(999L, List.of(new ItemPedidoRequest(1L, 1)))))
                .isInstanceOf(ReferenciaInvalidaException.class);

        // Si el cliente no existe, no se guarda nada.
        verify(repository, never()).save(any());
    }

    @Test
    void crearRechazaCuandoNoHayStockSuficiente() {
        when(catalogoClient.buscarProducto(1L))
                .thenReturn(new ProductoResponse(1L, "Polera", new BigDecimal("10000"), 1, true));

        assertThatThrownBy(() ->
                service.crear(new PedidoRequest(1L, List.of(new ItemPedidoRequest(1L, 3)))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(repository, never()).save(any());
    }
}
