package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out.ReservaVacanteExternalService;

@Component
@RequiredArgsConstructor
public class ReservaVacanteFeignClientAdapter implements ReservaVacanteExternalService {

    private final ReservaVacanteFeignClient feignClient;

    @Override
    public ReservaVacante create(ReservaVacante domain) {
        BackendReservaVacanteRequest request = BackendReservaVacanteRequest.builder()
                .tipoDocumento(domain.getTipoDocumento())
                .numeroDocumento(domain.getNumeroDocumento())
                .nombre(domain.getNombre())
                .apellido(domain.getApellido())
                .email(domain.getEmail())
                .campanhaId(domain.getCampanhaId())
                .build();

        BackendReservaVacanteResponse response = feignClient.createReservaVacante(request);

        if (response == null) {
            return null;
        }

        return ReservaVacante.builder()
                .id(response.getReservaVacanteId())
                .tipoDocumento(response.getTipoDocumento())
                .numeroDocumento(response.getNumeroDocumento())
                .nombre(response.getNombre())
                .apellido(response.getApellido())
                .email(response.getEmail())
                .campanhaId(response.getCampanhaId())
                .status(response.getEstado())
                .creadoEn(response.getCreated())
                .actualizadoEn(response.getUpdated())
                .build();
    }
}
