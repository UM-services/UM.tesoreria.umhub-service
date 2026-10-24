package tesoreria.umhub.configuration;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka.PaymentProcessedEvent;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El contrato de {@code payment-processed} es el JSON del evento: se deserializa por tipo target
 * y no depende de {@code __TypeId__}.
 */
class KafkaConsumerConfigTest {

    private static final String TOPIC = "payment-processed";

    private DefaultKafkaConsumerFactory<String, PaymentProcessedEvent> factory;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        var config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "kafka:9092");
        ReflectionTestUtils.setField(config, "groupId", "tesoreria-umhub-group");
        factory = (DefaultKafkaConsumerFactory<String, PaymentProcessedEvent>) config.consumerFactory();
    }

    @Test
    void deserializaElEventoSinHeadersDeTipo() {
        var event = deserializar(new RecordHeaders());

        assertThat(event.getPaymentId()).isEqualTo("12345");
        assertThat(event.getStatus()).isEqualTo("approved");
        assertThat(event.getTransactionAmount()).isEqualByComparingTo("1234.50");
        assertThat(event.getDateApproved())
                .isEqualTo(OffsetDateTime.of(2026, 10, 20, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void ignoraElTypeIdDelProductorAnterior() {
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "um.tesoreria.mercadopago.service.domain.event.PaymentProcessedEvent"
                .getBytes(StandardCharsets.UTF_8));

        assertThat(deserializar(headers).getPaymentId()).isEqualTo("12345");
    }

    @Test
    void ignoraUnTypeIdDeUnPaqueteDesconocido() {
        Headers headers = new RecordHeaders();
        headers.add("__TypeId__", "com.desconocido.Evento".getBytes(StandardCharsets.UTF_8));

        assertThat(deserializar(headers).getPaymentId()).isEqualTo("12345");
    }

    private PaymentProcessedEvent deserializar(Headers headers) {
        return factory.getValueDeserializer()
                .deserialize(TOPIC, headers, payload());
    }

    private byte[] payload() {
        return """
                {"mercadoPagoContextId":5,"chequeraCuotaId":7,"paymentId":"12345",
                 "status":"approved","statusDetail":"accredited",
                 "dateApproved":"2026-10-20T12:00:00Z","dateCreated":"2026-10-20T11:00:00Z",
                 "transactionAmount":1234.50,"paymentJson":"{\\"id\\":12345}","source":"WEBHOOK_V1"}
                """.getBytes(StandardCharsets.UTF_8);
    }

}
