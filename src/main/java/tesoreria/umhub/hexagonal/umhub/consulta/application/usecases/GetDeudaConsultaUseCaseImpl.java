package tesoreria.umhub.hexagonal.umhub.consulta.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.in.GetDeudaConsultaUseCase;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.out.ConsultaExternalService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetDeudaConsultaUseCaseImpl implements GetDeudaConsultaUseCase {

    private final ConsultaExternalService consultaExternalService;

    @Override
    public Optional<DeudaConsulta> findByNumeroDocumento(String numeroDocumento, boolean extended) {
        return consultaExternalService.getDeuda(numeroDocumento, extended);
    }
}
