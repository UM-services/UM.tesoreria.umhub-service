package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class N8nNotificationPayload {
    private UUID reservaVacanteId;
    private Integer tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String email;
    private UUID campanhaId;
    private BigDecimal importe;
    private String initPoint;
    private String status;
    private PagoDetails pago;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagoDetails {
        private String paymentId;
        private String status;
        private String statusDetail;
        private BigDecimal transactionAmount;
        private OffsetDateTime dateApproved;
        private OffsetDateTime dateCreated;
    }
}
