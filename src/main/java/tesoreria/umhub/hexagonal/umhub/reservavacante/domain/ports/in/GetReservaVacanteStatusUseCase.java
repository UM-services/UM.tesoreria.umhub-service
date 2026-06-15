package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in;

import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;

import java.util.UUID;

public interface GetReservaVacanteStatusUseCase {
    ReservaVacante getStatus(UUID reservaVacanteId);
}
