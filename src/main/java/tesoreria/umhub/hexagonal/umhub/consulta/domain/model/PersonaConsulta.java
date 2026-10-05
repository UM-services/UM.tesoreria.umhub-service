package tesoreria.umhub.hexagonal.umhub.consulta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Datos personales + domicilio/contacto resueltos por numero de documento
 * (sin tipo: el backend agrega los tipos del mismo titular).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaConsulta {
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String sexo;
    private String numeroPrefijo;
    private String numeroPosfijo;
    private List<TipoDocumentoConsulta> documentos;
    private DomicilioConsulta domicilio;
}
