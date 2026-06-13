package tesoreria.umhub.hexagonal.umhub.campanha.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campanha {
    private UUID id;
    private String nombre;
    private String estado;
    private LocalDateTime creadoEn;
}
