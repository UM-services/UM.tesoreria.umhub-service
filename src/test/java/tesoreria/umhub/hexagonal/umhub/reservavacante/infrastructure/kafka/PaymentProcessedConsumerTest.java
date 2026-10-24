package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tesoreria.umhub.hexagonal.umhub.reservavacante.application.service.ReservaVacanteService;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificacionClaimPort;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteNotificationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Idempotencia de la notificación: sólo notifica la entrega que gana el claim; un envío fallido
 * libera el claim y propaga la excepción para que Kafka reintente y derive al DLT.
 */
@ExtendWith(MockitoExtension.class)
class PaymentProcessedConsumerTest {

    private static final UUID RESERVA_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Mock
    private ReservaVacanteService reservaVacanteService;
    @Mock
    private ReservaVacanteNotificationService notificationService;
    @Mock
    private ReservaVacanteNotificacionClaimPort claimPort;

    @InjectMocks
    private PaymentProcessedConsumer consumer;

    @Test
    void unEventoSinReservaVacanteNoTocaElClaim() {
        consumer.consume(evento(null, "approved"));

        verifyNoInteractions(claimPort);
        verifyNoInteractions(notificationService);
    }

    @Test
    void unPagoNoAprobadoNoNotifica() {
        consumer.consume(evento(RESERVA_ID, "pending"));

        verifyNoInteractions(claimPort);
        verifyNoInteractions(notificationService);
    }

    @Test
    void siOtraEntregaYaReclamoLaNotificacionEsUnNoOp() {
        when(claimPort.claim(RESERVA_ID)).thenReturn(false);

        consumer.consume(evento(RESERVA_ID, "approved"));

        verifyNoInteractions(notificationService);
    }

    @Test
    void laEntregaQueGanaElClaimNotifica() {
        when(claimPort.claim(RESERVA_ID)).thenReturn(true);
        when(reservaVacanteService.getStatus(RESERVA_ID)).thenReturn(reserva());

        consumer.consume(evento(RESERVA_ID, "approved"));

        verify(notificationService).notifyPayment(any(), any());
        verify(claimPort, never()).release(any());
    }

    @Test
    void unEnvioFallidoLiberaElClaimYPropagaParaQueKafkaReintente() {
        when(claimPort.claim(RESERVA_ID)).thenReturn(true);
        when(reservaVacanteService.getStatus(RESERVA_ID)).thenReturn(reserva());
        var error = new IllegalStateException("n8n caido");
        doThrow(error).when(notificationService).notifyPayment(any(), any());

        assertThatThrownBy(() -> consumer.consume(evento(RESERVA_ID, "approved")))
                .isSameAs(error);

        verify(claimPort).release(RESERVA_ID);
    }

    @Test
    void siLaLiberacionTambienFallaQuedaComoExcepcionSuprimida() {
        when(claimPort.claim(RESERVA_ID)).thenReturn(true);
        when(reservaVacanteService.getStatus(RESERVA_ID)).thenReturn(reserva());
        var errorEnvio = new IllegalStateException("n8n caido");
        doThrow(errorEnvio).when(notificationService).notifyPayment(any(), any());
        var errorLiberacion = new IllegalStateException("core caido");
        doThrow(errorLiberacion).when(claimPort).release(RESERVA_ID);

        assertThatThrownBy(() -> consumer.consume(evento(RESERVA_ID, "approved")))
                .isSameAs(errorEnvio)
                .hasSuppressedException(errorLiberacion);
    }

    @Test
    void unaReservaInexistenteLiberaElClaimYPropaga() {
        when(claimPort.claim(RESERVA_ID)).thenReturn(true);
        when(reservaVacanteService.getStatus(RESERVA_ID)).thenReturn(null);

        assertThatThrownBy(() -> consumer.consume(evento(RESERVA_ID, "approved")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No existe la reserva");

        verify(claimPort).release(RESERVA_ID);
        verifyNoInteractions(notificationService);
    }

    private PaymentProcessedEvent evento(UUID reservaVacanteId, String status) {
        return PaymentProcessedEvent.builder()
                .reservaVacanteId(reservaVacanteId)
                .paymentId("12345")
                .status(status)
                .transactionAmount(new BigDecimal("1500.00"))
                .dateApproved(OffsetDateTime.of(2026, 10, 21, 12, 0, 0, 0, ZoneOffset.UTC))
                .build();
    }

    private ReservaVacante reserva() {
        return ReservaVacante.builder()
                .id(RESERVA_ID)
                .status("pendiente")
                .importe(new BigDecimal("1500.00"))
                .build();
    }

}
