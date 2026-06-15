package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanhaWrappedResponse {
    private boolean success;
    private CampanhaData data;
    private String mensaje;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampanhaData {
        private UUID id;
        private String nombre;
        private BigDecimal valorReserva;
        private String estado;

        @JsonProperty("creado_en")
        private String creadoEn;
    }
}
