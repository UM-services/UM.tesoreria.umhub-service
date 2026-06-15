package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out;

import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;

public interface ReservaVacanteExternalService {
    ReservaVacante create(ReservaVacante reservaVacante);
}
