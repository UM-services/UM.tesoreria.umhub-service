package tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.in;

import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;

import java.util.Optional;

public interface GetDeudaConsultaUseCase {

    Optional<DeudaConsulta> findByNumeroDocumento(String numeroDocumento, boolean extended);
}
