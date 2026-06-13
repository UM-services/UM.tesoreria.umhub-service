package tesoreria.umhub.hexagonal.umhub.campanha.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.in.UpdateCampanhaUseCase;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.out.CampanhaExternalService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateCampanhaUseCaseImpl implements UpdateCampanhaUseCase {

    private final CampanhaExternalService externalService;

    @Override
    public Campanha updateCampanha(UUID id, Campanha campanha) {
        return externalService.update(id, campanha);
    }
}
