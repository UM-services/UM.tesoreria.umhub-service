package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.dto.CampanhaRequest;
import tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.dto.CampanhaWrappedResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CampanhaDtoMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public Campanha toDomain(CampanhaRequest request) {
        if (request == null) return null;
        return Campanha.builder()
                .nombre(request.getNombre())
                .build();
    }

    public CampanhaWrappedResponse toWrappedResponse(Campanha domain, String message) {
        if (domain == null) return null;

        LocalDateTime createdTime = domain.getCreadoEn();
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        String formattedDate = createdTime.format(FORMATTER);

        CampanhaWrappedResponse.CampanhaData data = CampanhaWrappedResponse.CampanhaData.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .estado(domain.getEstado() != null ? domain.getEstado() : "activa")
                .creadoEn(formattedDate)
                .build();

        return CampanhaWrappedResponse.builder()
                .success(true)
                .data(data)
                .mensaje(message)
                .build();
    }
}
