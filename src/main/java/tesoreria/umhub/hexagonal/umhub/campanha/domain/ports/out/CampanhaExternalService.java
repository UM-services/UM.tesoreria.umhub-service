package tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.out;

import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import java.util.UUID;

public interface CampanhaExternalService {
    Campanha create(Campanha campanha);
    Campanha update(UUID id, Campanha campanha);
}
