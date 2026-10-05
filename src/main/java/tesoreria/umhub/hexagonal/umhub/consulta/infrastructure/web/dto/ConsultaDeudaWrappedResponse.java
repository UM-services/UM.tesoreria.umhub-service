package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaDeudaWrappedResponse {
    private boolean success;
    private DeudaData data;
    private String mensaje;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeudaData {
        @JsonProperty("numero_documento")
        private String numeroDocumento;

        private Integer cuotas;

        @JsonProperty("deuda_total")
        private BigDecimal deudaTotal;

        private List<DeudaChequeraData> deudas;
        private List<VencimientoData> vencimientos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeudaChequeraData {
        @JsonProperty("facultad_id")
        private Integer facultadId;

        private String facultad;

        @JsonProperty("tipo_chequera_id")
        private Integer tipoChequeraId;

        @JsonProperty("tipo_chequera")
        private String tipoChequera;

        @JsonProperty("chequera_serie_id")
        private Long chequeraSerieId;

        @JsonProperty("lectivo_id")
        private Integer lectivoId;

        private String lectivo;

        @JsonProperty("alternativa_id")
        private Integer alternativaId;

        private BigDecimal total;
        private BigDecimal deuda;
        private Integer cuotas;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
        private OffsetDateTime vencimiento;

        private BigDecimal importe;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VencimientoData {
        private String producto;
        private String periodo;
        private String vencimiento;
        private BigDecimal importe;

        @JsonProperty("init_point")
        private String initPoint;
    }
}
