package tesoreria.umhub.hexagonal.umhub.reservavacante.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in.CreateReservaVacanteUseCase;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in.GetReservaVacanteStatusUseCase;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaVacanteService {

    private final CreateReservaVacanteUseCase createReservaVacanteUseCase;
    private final GetReservaVacanteStatusUseCase getReservaVacanteStatusUseCase;

    public ReservaVacante createReservaVacante(ReservaVacante reservaVacante) {
        return createReservaVacanteUseCase.createReservaVacante(reservaVacante);
    }

    public ReservaVacante getStatus(UUID reservaVacanteId) {
        return getReservaVacanteStatusUseCase.getStatus(reservaVacanteId);
    }
}
