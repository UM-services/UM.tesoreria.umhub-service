package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.application.service.ReservaVacanteService;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessedConsumer {

    private final ReservaVacanteService reservaVacanteService;
    private final ReservaVacanteNotificationService notificationService;

    @KafkaListener(topics = "payment-processed", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent in tesoreria-umhub: {}", event);

        if (event.getReservaVacanteId() == null) {
            log.debug("Ignoring event as it is not a vacancy reservation payment (reservaVacanteId is null)");
            return;
        }

        if (!"approved".equalsIgnoreCase(event.getStatus())) {
            log.info("Ignoring event with status: {} (only approved payments are notified)", event.getStatus());
            return;
        }

        log.info("Processing vacancy reservation payment notification for reservaVacanteId: {}", event.getReservaVacanteId());

        try {
            ReservaVacante reservaVacante = reservaVacanteService.getStatus(event.getReservaVacanteId());
            if (reservaVacante == null) {
                log.error("Could not find ReservaVacante with ID: {} from backend service", event.getReservaVacanteId());
                return;
            }

            // Asegurar que informamos el estado correcto del pago en la notificación
            reservaVacante.setStatus("pagado");

            notificationService.notifyPayment(reservaVacante, event);
        } catch (Exception e) {
            log.error("Error processing PaymentProcessedEvent for reservaVacanteId: {}. Error: {}", 
                    event.getReservaVacanteId(), e.getMessage(), e);
        }
    }
}
