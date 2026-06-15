package tesoreria.umhub.hexagonal.umhub.reservavacante.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in.CreateReservaVacanteUseCase;

@Service
@RequiredArgsConstructor
public class ReservaVacanteService {

    private final CreateReservaVacanteUseCase createReservaVacanteUseCase;

    public ReservaVacante createReservaVacante(ReservaVacante reservaVacante) {
        return createReservaVacanteUseCase.createReservaVacante(reservaVacante);
    }
}
