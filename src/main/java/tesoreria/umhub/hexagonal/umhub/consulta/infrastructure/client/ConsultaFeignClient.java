package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tesoreria-core-service", contextId = "consultaClient",
        configuration = ConsultaFeignClientConfig.class)
public interface ConsultaFeignClient {

    @GetMapping("/api/tesoreria/core/umhub/consulta/persona/{numeroDocumento}")
    BackendConsultaPersonaResponse getPersona(@PathVariable("numeroDocumento") String numeroDocumento);

    @GetMapping("/api/tesoreria/core/umhub/consulta/persona/{numeroDocumento}/deuda")
    BackendConsultaDeudaResponse getDeuda(@PathVariable("numeroDocumento") String numeroDocumento,
                                          @RequestParam("extended") boolean extended);
}
