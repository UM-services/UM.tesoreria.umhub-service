package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out;

import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka.PaymentProcessedEvent;

public interface ReservaVacanteNotificationService {
    void notifyPayment(ReservaVacante reservaVacante, PaymentProcessedEvent event);
}
