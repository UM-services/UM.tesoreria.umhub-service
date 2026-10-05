package tesoreria.umhub.hexagonal.umhub.consulta.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.in.GetDeudaConsultaUseCase;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.in.GetPersonaConsultaUseCase;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final GetPersonaConsultaUseCase getPersonaConsultaUseCase;
    private final GetDeudaConsultaUseCase getDeudaConsultaUseCase;

    public Optional<PersonaConsulta> findPersona(String numeroDocumento) {
        return getPersonaConsultaUseCase.findByNumeroDocumento(numeroDocumento);
    }

    public Optional<DeudaConsulta> findDeuda(String numeroDocumento, boolean extended) {
        return getDeudaConsultaUseCase.findByNumeroDocumento(numeroDocumento, extended);
    }
}
