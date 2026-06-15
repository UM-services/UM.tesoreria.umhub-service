package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaVacante {
    private UUID id;
    private Integer tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String email;
    private UUID campanhaId;
    private String status;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
