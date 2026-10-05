package tesoreria.umhub.hexagonal.umhub.consulta.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.in.GetPersonaConsultaUseCase;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.out.ConsultaExternalService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetPersonaConsultaUseCaseImpl implements GetPersonaConsultaUseCase {

    private final ConsultaExternalService consultaExternalService;

    @Override
    public Optional<PersonaConsulta> findByNumeroDocumento(String numeroDocumento) {
        return consultaExternalService.getPersona(numeroDocumento);
    }
}
