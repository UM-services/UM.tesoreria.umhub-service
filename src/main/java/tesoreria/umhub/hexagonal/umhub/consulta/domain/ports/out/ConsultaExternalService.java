package tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.out;

import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;

import java.util.Optional;

/**
 * Puerto de salida hacia tesoreria-core (slice /umhub/consulta).
 * Optional.empty() indica "persona no encontrada" (404 del backend).
 */
public interface ConsultaExternalService {

    Optional<PersonaConsulta> getPersona(String numeroDocumento);

    Optional<DeudaConsulta> getDeuda(String numeroDocumento, boolean extended);
}
