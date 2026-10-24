package tesoreria.umhub.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.kafka.PaymentProcessedEvent;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    /**
     * Reintentos antes de derivar a {@code payment-processed.DLT}. Mismo criterio que en core:
     * 1 s inicial y factor 2, con un techo de ~16 s para el último intento.
     */
    static final long REINTENTOS_PAGO_PROCESADO = 5L;
    static final long ESPERA_INICIAL_PAGO_PROCESADO = 1000L;
    static final double FACTOR_PAGO_PROCESADO = 2.0;

    static final String SUFIJO_DLT = ".DLT";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, PaymentProcessedEvent> consumerFactory() {
        log.info("Initializing Kafka Consumer Factory in tesoreria-umhub with bootstrapServers: {} and groupId: {}", bootstrapServers, groupId);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        JsonMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        // El contrato es el JSON del evento: el tipo target manda y __TypeId__ se ignora.
        var jsonDeserializer = new JacksonJsonDeserializer<>(PaymentProcessedEvent.class, objectMapper, false);
        var errorHandlingDeserializer = new ErrorHandlingDeserializer<PaymentProcessedEvent>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(defaultErrorHandler(kafkaTemplate, dltKafkaTemplate));
        return factory;
    }

    /**
     * Reintentos con backoff exponencial y derivación a {@code payment-processed.DLT} cuando se
     * agotan. El template se elige por tipo de valor: evento ya deserializado → JSON; record que no
     * se pudo deserializar → bytes crudos.
     */
    DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                            KafkaTemplate<String, byte[]> dltKafkaTemplate) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new HashMap<>();
        templates.put(PaymentProcessedEvent.class, kafkaTemplate);
        templates.put(byte[].class, dltKafkaTemplate);

        var recoverer = new DeadLetterPublishingRecoverer(templates, KafkaConsumerConfig::destinoDlt);
        /*
         * Un JSON inválido (DeserializationException) ya viene clasificado como fatal por Spring
         * Kafka: no consume reintentos y va directo al DLT.
         */
        return new DefaultErrorHandler(recoverer, backOffPagoProcesado());
    }

    static ExponentialBackOff backOffPagoProcesado() {
        var backOff = new ExponentialBackOff(ESPERA_INICIAL_PAGO_PROCESADO, FACTOR_PAGO_PROCESADO);
        backOff.setMaxAttempts(REINTENTOS_PAGO_PROCESADO);
        return backOff;
    }

    static TopicPartition destinoDlt(ConsumerRecord<?, ?> record, Exception exception) {
        return new TopicPartition(record.topic() + SUFIJO_DLT, record.partition());
    }
}
