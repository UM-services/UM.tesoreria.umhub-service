package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackendCampanhaResponse {
    private UUID campanhaId;
    private String nombre;
    private Byte activa;
    private LocalDateTime created;
}
