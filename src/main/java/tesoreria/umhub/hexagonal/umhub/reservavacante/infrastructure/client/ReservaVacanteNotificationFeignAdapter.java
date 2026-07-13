package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificationService;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka.PaymentProcessedEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaVacanteNotificationFeignAdapter implements ReservaVacanteNotificationService {

    private final ReservaVacanteNotificationFeignClient feignClient;

    @Override
    public void notifyPayment(ReservaVacante domain, PaymentProcessedEvent event) {
        log.info("Sending payment notification to n8n webhook for reservaVacanteId: {}", domain.getId());
        
        N8nNotificationPayload.PagoDetails pagoDetails = N8nNotificationPayload.PagoDetails.builder()
                .paymentId(event.getPaymentId())
                .status(event.getStatus())
                .statusDetail(event.getStatusDetail())
                .transactionAmount(event.getTransactionAmount())
                .dateApproved(event.getDateApproved())
                .dateCreated(event.getDateCreated())
                .build();

        N8nNotificationPayload payload = N8nNotificationPayload.builder()
                .reservaVacanteId(domain.getId())
                .tipoDocumento(domain.getTipoDocumento())
                .numeroDocumento(domain.getNumeroDocumento())
                .nombre(domain.getNombre())
                .apellido(domain.getApellido())
                .email(domain.getEmail())
                .campanhaId(domain.getCampanhaId())
                .importe(domain.getImporte())
                .initPoint(domain.getInitPoint())
                .status(domain.getStatus())
                .pago(pagoDetails)
                .build();

        try {
            feignClient.notifyPayment(payload);
            log.info("Successfully notified n8n webhook for payment of reservaVacanteId: {}", domain.getId());
        } catch (Exception e) {
            log.error("Failed to notify n8n webhook for payment of reservaVacanteId: {}. Error: {}", domain.getId(), e.getMessage(), e);
        }
    }
}
