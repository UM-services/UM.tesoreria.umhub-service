package tesoreria.umhub.hexagonal.umhub.reservavacante.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in.CreateReservaVacanteUseCase;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteExternalService;

@Component
@RequiredArgsConstructor
public class CreateReservaVacanteUseCaseImpl implements CreateReservaVacanteUseCase {

    private final ReservaVacanteExternalService externalService;

    @Override
    public ReservaVacante createReservaVacante(ReservaVacante reservaVacante) {
        return externalService.create(reservaVacante);
    }
}
