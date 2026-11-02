package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.application.service.ReservaVacanteService;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificacionClaimPort;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificationService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessedConsumer {

    private final ReservaVacanteService reservaVacanteService;
    private final ReservaVacanteNotificationService notificationService;
    private final ReservaVacanteNotificacionClaimPort claimPort;

    /**
     * Idempotente por el claim de la reserva en core: si otra entrega del mismo evento ya notificó,
     * esta es un no-op. Si el envío falla se libera el claim y la excepción se propaga, para que
     * Kafka reintente con backoff y, agotados los intentos, derive a {@code payment-processed.DLT}.
     */
    @KafkaListener(topics = "payment-processed", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(PaymentProcessedEvent event) {
        log.debug("Received PaymentProcessedEvent in tesoreria-umhub: {}", event);

        if (event.getReservaVacanteId() == null) {
            log.debug("Ignoring event as it is not a vacancy reservation payment (reservaVacanteId is null)");
            return;
        }

        if (!"approved".equalsIgnoreCase(event.getStatus())) {
            log.info("Ignoring event with status: {} (only approved payments are notified)", event.getStatus());
            return;
        }

        if (!claimPort.claim(event.getReservaVacanteId())) {
            log.info("Notificación de pago ya enviada para reserva {} (paymentId={}): no-op",
                    event.getReservaVacanteId(), event.getPaymentId());
            return;
        }

        try {
            log.info("Processing vacancy reservation payment notification for reservaVacanteId: {}",
                    event.getReservaVacanteId());

            ReservaVacante reservaVacante = reservaVacanteService.getStatus(event.getReservaVacanteId());
            if (reservaVacante == null) {
                throw new IllegalStateException("No existe la reserva de vacante "
                        + event.getReservaVacanteId() + " en el core");
            }

            // Asegurar que informamos el estado correcto del pago en la notificación
            reservaVacante.setStatus("pagado");

            notificationService.notifyPayment(reservaVacante, event);
        } catch (RuntimeException e) {
            liberarClaim(event.getReservaVacanteId(), e);
            throw e;
        }
    }

    private void liberarClaim(UUID reservaVacanteId, RuntimeException causa) {
        try {
            claimPort.release(reservaVacanteId);
            log.warn("Claim de notificación liberado para reserva {}: el reintento de Kafka vuelve a intentar el envío",
                    reservaVacanteId);
        } catch (RuntimeException errorLiberacion) {
            causa.addSuppressed(errorLiberacion);
            log.error("No se pudo liberar el claim de notificación de la reserva {}: queda tomado y requiere intervención manual",
                    reservaVacanteId, errorLiberacion);
        }
    }

}
