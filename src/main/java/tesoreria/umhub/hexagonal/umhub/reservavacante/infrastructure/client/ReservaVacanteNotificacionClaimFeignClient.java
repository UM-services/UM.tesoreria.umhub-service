package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

/**
 * Endpoints internos de claim/release que expone core en el slice de reserva de vacante.
 */
@FeignClient(name = "tesoreria-core-service", contextId = "reservaVacanteNotificacionClaimClient")
public interface ReservaVacanteNotificacionClaimFeignClient {

    @PostMapping("/api/tesoreria/core/umhub/reservaVacante/{reservaVacanteId}/notificacion/claim")
    Boolean claim(@PathVariable UUID reservaVacanteId);

    @PostMapping("/api/tesoreria/core/umhub/reservaVacante/{reservaVacanteId}/notificacion/release")
    void release(@PathVariable UUID reservaVacanteId);

}
