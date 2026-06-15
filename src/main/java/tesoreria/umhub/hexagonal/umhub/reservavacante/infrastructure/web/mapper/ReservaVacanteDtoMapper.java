package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.dto.ReservaVacanteRequest;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.dto.ReservaVacanteWrappedResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ReservaVacanteDtoMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public ReservaVacante toDomain(ReservaVacanteRequest request) {
        if (request == null) return null;
        return ReservaVacante.builder()
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .campanhaId(request.getCampanhaId())
                .build();
    }

    public ReservaVacanteWrappedResponse toWrappedResponse(ReservaVacante domain, String message) {
        if (domain == null) return null;

        String creadoEnStr = domain.getCreadoEn() != null ? domain.getCreadoEn().format(FORMATTER) : null;
        String actualizadoEnStr = domain.getActualizadoEn() != null ? domain.getActualizadoEn().format(FORMATTER) : null;

        ReservaVacanteWrappedResponse.ReservaVacanteData data = ReservaVacanteWrappedResponse.ReservaVacanteData.builder()
                .id(domain.getId())
                .tipoDocumento(domain.getTipoDocumento())
                .numeroDocumento(domain.getNumeroDocumento())
                .nombre(domain.getNombre())
                .apellido(domain.getApellido())
                .email(domain.getEmail())
                .campanhaId(domain.getCampanhaId())
                .status(domain.getStatus() != null ? domain.getStatus() : "pendiente")
                .creadoEn(creadoEnStr)
                .actualizadoEn(actualizadoEnStr)
                .build();

        return ReservaVacanteWrappedResponse.builder()
                .success(true)
                .data(data)
                .mensaje(message)
                .build();
    }
}
