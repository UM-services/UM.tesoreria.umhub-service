package tesoreria.umhub.hexagonal.umhub.consulta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deuda agregada por numero de documento, sobre todos los tipos del mismo titular.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaConsulta {
    private String numeroDocumento;
    private Integer cuotas;
    private BigDecimal deudaTotal;
    private List<DeudaChequera> deudas;
    private List<VencimientoConsulta> vencimientos;
}
