package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackendReservaVacanteResponse {
    private UUID reservaVacanteId;
    private Integer tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String email;
    private UUID campanhaId;
    private String estado;
    private BigDecimal importe;
    private String initPoint;
    private LocalDateTime created;
    private LocalDateTime updated;
}
