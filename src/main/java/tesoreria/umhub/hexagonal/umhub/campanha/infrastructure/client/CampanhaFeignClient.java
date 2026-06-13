package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "tesoreria-core-service")
public interface CampanhaFeignClient {

    @PostMapping("/api/tesoreria/umhub/campanha/")
    BackendCampanhaResponse createCampanha(@RequestBody BackendCampanhaRequest request);

    @PutMapping("/api/tesoreria/umhub/campanha/{campanhaId}")
    BackendCampanhaResponse updateCampanha(@PathVariable("campanhaId") UUID campanhaId, @RequestBody BackendCampanhaRequest request);
}
