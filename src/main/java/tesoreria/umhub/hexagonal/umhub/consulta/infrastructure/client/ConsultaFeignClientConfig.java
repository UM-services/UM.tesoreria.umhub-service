package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.client;

import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * Configuracion Feign exclusiva del contexto "consultaClient" (clase sin @Configuration
 * para no afectar a los demas clientes). Endpoints de consulta sincronos: read 15 s,
 * sin heredar los 200 s globales usados por MercadoPago.
 */
public class ConsultaFeignClientConfig {

    @Bean
    public Request.Options consultaRequestOptions() {
        return new Request.Options(5, TimeUnit.SECONDS, 15, TimeUnit.SECONDS, true);
    }
}
