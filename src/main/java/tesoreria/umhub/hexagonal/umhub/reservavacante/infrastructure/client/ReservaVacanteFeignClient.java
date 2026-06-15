package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "tesoreria-core-service", contextId = "reservaVacanteClient")
public interface ReservaVacanteFeignClient {

    @PostMapping("/api/tesoreria/core/umhub/reservaVacante/")
    BackendReservaVacanteResponse createReservaVacante(@RequestBody BackendReservaVacanteRequest request);

    @GetMapping("/api/tesoreria/core/umhub/reservaVacante/{reservaVacanteId}")
    BackendReservaVacanteResponse getReservaVacanteStatus(@PathVariable UUID reservaVacanteId);
}
