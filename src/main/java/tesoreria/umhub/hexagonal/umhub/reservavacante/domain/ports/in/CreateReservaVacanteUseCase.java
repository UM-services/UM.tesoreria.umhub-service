package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.in;

import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;

public interface CreateReservaVacanteUseCase {
    ReservaVacante createReservaVacante(ReservaVacante reservaVacante);
}
