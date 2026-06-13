package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.out.CampanhaExternalService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CampanhaFeignClientAdapter implements CampanhaExternalService {

    private final CampanhaFeignClient feignClient;

    @Override
    public Campanha create(Campanha campanha) {
        BackendCampanhaRequest request = BackendCampanhaRequest.builder()
                .campanhaId(campanha.getId())
                .nombre(campanha.getNombre())
                .activa((byte) 1) // Default to active (1)
                .build();

        BackendCampanhaResponse response = feignClient.createCampanha(request);

        if (response == null) {
            return null;
        }

        return Campanha.builder()
                .id(response.getCampanhaId())
                .nombre(response.getNombre())
                .estado(response.getActiva() != null && response.getActiva() == 1 ? "activa" : "inactiva")
                .creadoEn(response.getCreated())
                .build();
    }

    @Override
    public Campanha update(UUID id, Campanha campanha) {
        BackendCampanhaRequest request = BackendCampanhaRequest.builder()
                .campanhaId(id)
                .nombre(campanha.getNombre())
                .activa((byte) 1) // Default to active (1)
                .build();

        BackendCampanhaResponse response = feignClient.updateCampanha(id, request);

        if (response == null) {
            return null;
        }

        return Campanha.builder()
                .id(response.getCampanhaId())
                .nombre(response.getNombre())
                .estado(response.getActiva() != null && response.getActiva() == 1 ? "activa" : "inactiva")
                .creadoEn(response.getCreated())
                .build();
    }
}
