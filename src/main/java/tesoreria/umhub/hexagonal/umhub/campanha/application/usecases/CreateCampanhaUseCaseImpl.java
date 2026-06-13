package tesoreria.umhub.hexagonal.umhub.campanha.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.in.CreateCampanhaUseCase;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.ports.out.CampanhaExternalService;

@Component
@RequiredArgsConstructor
public class CreateCampanhaUseCaseImpl implements CreateCampanhaUseCase {

    private final CampanhaExternalService externalService;

    @Override
    public Campanha createCampanha(Campanha campanha) {
        return externalService.create(campanha);
    }
}
