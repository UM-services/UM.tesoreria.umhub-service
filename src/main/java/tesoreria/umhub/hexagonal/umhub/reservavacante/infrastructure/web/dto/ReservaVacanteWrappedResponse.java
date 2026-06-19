package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.dto;

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
public class ReservaVacanteWrappedResponse {
    private boolean success;
    private ReservaVacanteData data;
    private String mensaje;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservaVacanteData {
        private UUID id;

        @JsonProperty("tipo_documento")
        private Integer tipoDocumento;

        @JsonProperty("numero_documento")
        private String numeroDocumento;

        private String nombre;
        private String apellido;
        private String email;

        @JsonProperty("campanha_id")
        private UUID campanhaId;

        private String status;

        @JsonProperty("init_point")
        private String initPoint;

        private BigDecimal importe;

        @JsonProperty("creado_en")
        private String creadoEn;

        @JsonProperty("actualizado_en")
        private String actualizadoEn;
    }
}
