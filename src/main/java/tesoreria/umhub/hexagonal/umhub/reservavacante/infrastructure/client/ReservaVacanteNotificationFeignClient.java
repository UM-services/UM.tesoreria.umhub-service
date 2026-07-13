package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "n8n-webhook-client", contextId = "n8nWebhookClient", url = "${app.n8n.webhook-url}")
public interface ReservaVacanteNotificationFeignClient {

    @PostMapping("/webhook/mercado-pago")
    void notifyPayment(@RequestBody N8nNotificationPayload payload);
}
