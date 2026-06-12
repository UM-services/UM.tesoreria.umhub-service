package tesoreria.umhub.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "tesoreria.umhub")
public class UmHubConfiguration {
}
