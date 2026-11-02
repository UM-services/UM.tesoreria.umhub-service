package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificacionClaimPort;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaVacanteNotificacionClaimFeignAdapter implements ReservaVacanteNotificacionClaimPort {

    private final ReservaVacanteNotificacionClaimFeignClient feignClient;

    @Override
    public boolean claim(UUID reservaVacanteId) {
        boolean reclamada = Boolean.TRUE.equals(feignClient.claim(reservaVacanteId));
        log.debug("Claim de notificación de pago para reserva {}: {}", reservaVacanteId, reclamada);
        return reclamada;
    }

    /**
     * No se captura el error: si la liberación falla, el consumer agrega la excepción como
     * suprimida del error original y deja que Kafka reintente. Tragarlo dejaría la marca tomada
     * para siempre y la notificación no se enviaría nunca.
     */
    @Override
    public void release(UUID reservaVacanteId) {
        feignClient.release(reservaVacanteId);
    }

}
