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
public class ReservaVacanteRequest {

    @JsonProperty("tipo_documento")
    private Integer tipoDocumento;

    @JsonProperty("numero_documento")
    private String numeroDocumento;

    private String nombre;
    private String apellido;
    private String email;

    @JsonProperty("campanha_id")
    private UUID campanhaId;

    private BigDecimal importe;
}
