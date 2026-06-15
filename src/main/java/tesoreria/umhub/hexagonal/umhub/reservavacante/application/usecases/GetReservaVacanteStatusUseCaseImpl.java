package tesoreria.umhub.hexagonal.umhub.reservavacante.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in.GetReservaVacanteStatusUseCase;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteExternalService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetReservaVacanteStatusUseCaseImpl implements GetReservaVacanteStatusUseCase {

    private final ReservaVacanteExternalService externalService;

    @Override
    public ReservaVacante getStatus(UUID reservaVacanteId) {
        return externalService.getStatus(reservaVacanteId);
    }
}
