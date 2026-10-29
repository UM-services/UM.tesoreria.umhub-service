package tesoreria.umhub.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Productor propio de umhub: sólo se usa para publicar en {@code payment-processed.DLT} (el
 * servicio no produce eventos de negocio).
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public JacksonJsonSerializer<Object> kafkaJsonSerializer() {
        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();
        // El contrato del topic es el JSON del evento: no se escribe __TypeId__.
        serializer.setAddTypeInfo(false);
        return serializer;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(JacksonJsonSerializer<Object> kafkaJsonSerializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // La instancia explícita es obligatoria: si se declara sólo la clase, Kafka la crea con
        // addTypeInfo=true y el DLT de umhub sale con __TypeId__ (header que apunta a una clase
        // que no existe en los demás servicios).
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), kafkaJsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(JacksonJsonSerializer<Object> kafkaJsonSerializer) {
        return new KafkaTemplate<>(producerFactory(kafkaJsonSerializer));
    }

    /**
     * Template binario para los records que no se pudieron deserializar: el
     * {@code DeadLetterPublishingRecoverer} republica los bytes originales y con el template JSON
     * terminarían en base64 dentro de un string.
     */
    @Bean
    public ProducerFactory<String, byte[]> dltProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, byte[]> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

}
