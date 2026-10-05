package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPersonaWrappedResponse {
    private boolean success;
    private PersonaData data;
    private String mensaje;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonaData {
        @JsonProperty("numero_documento")
        private String numeroDocumento;

        private String nombre;
        private String apellido;
        private String sexo;

        @JsonProperty("numero_prefijo")
        private String numeroPrefijo;

        @JsonProperty("numero_posfijo")
        private String numeroPosfijo;

        private List<TipoDocumentoData> documentos;
        private ContactoData contacto;
        private DomicilioData domicilio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipoDocumentoData {
        @JsonProperty("tipo_documento_id")
        private Integer tipoDocumentoId;

        @JsonProperty("tipo_documento")
        private String tipoDocumento;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContactoData {
        @JsonProperty("email_personal")
        private String emailPersonal;

        @JsonProperty("email_institucional")
        private String emailInstitucional;

        private String telefono;
        private String movil;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomicilioData {
        private String calle;
        private String puerta;
        private String piso;
        private String dpto;

        @JsonProperty("codigo_postal")
        private String codigoPostal;

        @JsonProperty("provincia_id")
        private Integer provinciaId;

        @JsonProperty("provincia_nombre")
        private String provinciaNombre;

        @JsonProperty("localidad_id")
        private Integer localidadId;

        @JsonProperty("localidad_nombre")
        private String localidadNombre;
    }
}
