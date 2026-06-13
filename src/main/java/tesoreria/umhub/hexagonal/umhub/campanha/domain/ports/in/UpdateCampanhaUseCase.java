package tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.in;

import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import java.util.UUID;

public interface UpdateCampanhaUseCase {
    Campanha updateCampanha(UUID id, Campanha campanha);
}
