package tesoreria.umhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.listener.auto-startup=false",
    // En tests no hay broker: sin KafkaAdmin no se espera el timeout de creación de topics.
    "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration"
})
class UmHubApplicationTests {

    @Test
    void contextLoads() {
    }

}
