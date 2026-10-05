package tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.in;

import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;

import java.util.Optional;

public interface GetPersonaConsultaUseCase {

    Optional<PersonaConsulta> findByNumeroDocumento(String numeroDocumento);
}
