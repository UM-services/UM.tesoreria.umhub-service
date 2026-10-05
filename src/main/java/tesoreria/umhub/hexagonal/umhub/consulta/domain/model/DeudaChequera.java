package tesoreria.umhub.hexagonal.umhub.consulta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Deuda de una chequera (fila del detalle publicado por el hub).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaChequera {
    private Integer facultadId;
    private String facultad;
    private Integer tipoChequeraId;
    private String tipoChequera;
    private Long chequeraSerieId;
    private Integer lectivoId;
    private String lectivo;
    private Integer alternativaId;
    private BigDecimal total;
    private BigDecimal deuda;
    private Integer cuotas;
    private OffsetDateTime vencimiento;
    private BigDecimal importe;
}
