package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampanhaRequest {
    private String nombre;
    private BigDecimal valorReserva;
}
