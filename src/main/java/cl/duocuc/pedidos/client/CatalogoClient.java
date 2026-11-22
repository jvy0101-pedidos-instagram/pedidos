package cl.duocuc.pedidos.client;

import cl.duocuc.pedidos.dto.ProductoResponse;
import cl.duocuc.pedidos.exception.ReferenciaInvalidaException;
import cl.duocuc.pedidos.exception.ServicioNoDisponibleException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Consulta al microservicio Catalogo por HTTP. */
@Component
public class CatalogoClient {

    private final RestClient restClient;

    // La URL viene de application.properties (o de la variable CATALOGO_URL en Docker).
    public CatalogoClient(@Value("${servicios.catalogo.url:http://localhost:8081}") String baseUrl) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(3));
        tiempos.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(tiempos)
                .build();
    }

    /** Devuelve el producto con su precio y stock, o lanza una excepcion si no existe. */
    public ProductoResponse buscarProducto(Long productoId) {
        try {
            return restClient.get()
                    .uri("/api/v1/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ReferenciaInvalidaException("No existe producto con id " + productoId);
                    })
                    .body(ProductoResponse.class);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException("El servicio Catalogo no responde");
        }
    }
}
