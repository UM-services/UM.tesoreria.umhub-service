package tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.in;

import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;

public interface CreateCampanhaUseCase {
    Campanha createCampanha(Campanha campanha);
}
