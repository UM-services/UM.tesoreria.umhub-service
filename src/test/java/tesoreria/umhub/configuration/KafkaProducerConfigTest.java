package tesoreria.umhub.configuration;

import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka.PaymentProcessedEvent;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El productor de umhub sólo escribe en {@code payment-processed.DLT}: el payload tiene que ser
 * el JSON del evento, sin {@code __TypeId__}. Si se configura sólo la clase del serializer, Kafka
 * la instancia con {@code addTypeInfo=true} y el header apunta a una clase que no existe en los
 * demás servicios.
 */
class KafkaProducerConfigTest {

    @Test
    void serializaSinTypeIdParaElDlt() {
        var config = new KafkaProducerConfig();
        var headers = new RecordHeaders();

        byte[] payload = config.kafkaJsonSerializer().serialize("payment-processed.DLT", headers, evento());

        assertThat(headers.lastHeader("__TypeId__")).isNull();
        var node = JsonMapper.builder().findAndAddModules().build()
                .readTree(new String(payload, StandardCharsets.UTF_8));
        assertThat(node.get("paymentId").asString()).isEqualTo("12345");
        assertThat(node.get("status").asString()).isEqualTo("approved");
        assertThat(node.get("transactionAmount").asDouble()).isEqualTo(1234.50);
    }

    @Test
    @SuppressWarnings("unchecked")
    void elFactoryUsaElSerializerDelBeanSinTypeInfo() {
        var config = new KafkaProducerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "kafka:9092");

        var factory = (DefaultKafkaProducerFactory<String, Object>)
                config.producerFactory(config.kafkaJsonSerializer());

        var serializer = (JacksonJsonSerializer<Object>) factory.getValueSerializerSupplier().get();
        assertThat(serializer.isAddTypeInfo()).isFalse();
    }

    private PaymentProcessedEvent evento() {
        return PaymentProcessedEvent.builder()
                .mercadoPagoContextId(5L)
                .chequeraCuotaId(7L)
                .reservaVacanteId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .paymentId("12345")
                .status("approved")
                .statusDetail("accredited")
                .dateApproved(OffsetDateTime.of(2026, 10, 20, 12, 0, 0, 0, ZoneOffset.UTC))
                .transactionAmount(new BigDecimal("1234.50"))
                .source("WEBHOOK_V1")
                .build();
    }

}
