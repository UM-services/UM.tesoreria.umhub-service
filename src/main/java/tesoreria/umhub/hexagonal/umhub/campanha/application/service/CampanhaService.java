package tesoreria.umhub.hexagonal.umhub.campanha.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.in.CreateCampanhaUseCase;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.in.UpdateCampanhaUseCase;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampanhaService {

    private final CreateCampanhaUseCase createCampanhaUseCase;
    private final UpdateCampanhaUseCase updateCampanhaUseCase;

    public Campanha createCampanha(Campanha campanha) {
        return createCampanhaUseCase.createCampanha(campanha);
    }

    public Campanha updateCampanha(UUID id, Campanha campanha) {
        return updateCampanhaUseCase.updateCampanha(id, campanha);
    }
}
