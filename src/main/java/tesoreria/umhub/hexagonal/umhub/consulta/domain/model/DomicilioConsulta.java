package tesoreria.umhub.hexagonal.umhub.consulta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domicilio + contacto publicado por el hub (subconjunto minimizado del backend:
 * nunca emailPagador ni laboral — son datos de terceros).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioConsulta {
    private String calle;
    private String puerta;
    private String piso;
    private String dpto;
    private String codigoPostal;
    private Integer provinciaId;
    private String provinciaNombre;
    private Integer localidadId;
    private String localidadNombre;
    private String telefono;
    private String movil;
    private String emailPersonal;
    private String emailInstitucional;
}
