package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Espejo del contrato JSON de tesoreria-core
 * GET /api/tesoreria/core/umhub/consulta/persona/{numeroDocumento}/deuda.
 * deudas[] replica los nombres de DeudaChequeraDto (vencimiento1/importe1 incluidos).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackendConsultaDeudaResponse {
    private String numeroDocumento;
    private Integer cuotas;
    private BigDecimal deuda;
    private List<BackendDeudaChequera> deudas;
    private List<BackendVencimiento> vencimientos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackendDeudaChequera {
        private BigDecimal personaId;
        private Integer documentoId;
        private Integer facultadId;
        private String facultad;
        private Integer tipochequeraId;
        private String tipochequera;
        private Long chequeraserieId;
        private Integer lectivoId;
        private String lectivo;
        private Integer alternativaId;
        private BigDecimal total;
        private BigDecimal deuda;
        private Integer cuotas;
        private Long chequeraId;
        private OffsetDateTime vencimiento1;
        private BigDecimal importe1;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackendVencimiento {
        private Long chequeraCuotaId;
        private Long mercadoPagoContextId;
        private String producto;
        private String periodo;
        private String vencimiento;
        private BigDecimal importe;
        private String initPoint;
    }
}
