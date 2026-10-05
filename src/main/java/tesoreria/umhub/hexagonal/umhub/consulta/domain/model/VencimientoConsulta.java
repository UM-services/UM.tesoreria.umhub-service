package tesoreria.umhub.hexagonal.umhub.consulta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Vencimiento de cuota en modo extendido (incluye init_point de MercadoPago).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VencimientoConsulta {
    private String producto;
    private String periodo;
    private String vencimiento;
    private BigDecimal importe;
    private String initPoint;
}
