package cl.duocuc.pedidos.client;

import cl.duocuc.pedidos.exception.ReferenciaInvalidaException;
import cl.duocuc.pedidos.exception.ServicioNoDisponibleException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Consulta al microservicio Clientes por HTTP. */
@Component
public class ClienteClient {

    private final RestClient restClient;

    // La URL viene de application.properties (o de la variable CLIENTES_URL en Docker).
    public ClienteClient(@Value("${servicios.clientes.url:http://localhost:8082}") String baseUrl) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(3));
        tiempos.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(tiempos)
                .build();
    }

    /** Lanza una excepcion si el cliente no existe; si existe, no hace nada. */
    public void verificarExiste(Long clienteId) {
        try {
            restClient.get()
                    .uri("/api/v1/clientes/{id}", clienteId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ReferenciaInvalidaException("No existe cliente con id " + clienteId);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException("El servicio Clientes no responde");
        }
    }
}
