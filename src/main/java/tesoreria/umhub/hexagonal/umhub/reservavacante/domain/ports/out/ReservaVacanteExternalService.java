package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out;

import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;

import java.util.UUID;

public interface ReservaVacanteExternalService {
    ReservaVacante create(ReservaVacante reservaVacante);
    ReservaVacante getStatus(UUID reservaVacanteId);
}
