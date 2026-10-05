package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Espejo del contrato JSON de tesoreria-core
 * GET /api/tesoreria/core/umhub/consulta/persona/{numeroDocumento}.
 * Campos en camelCase: el snake_case del contrato publico se aplica en web/dto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackendConsultaPersonaResponse {
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String sexo;
    private String numeroPrefijo;
    private String numeroPosfijo;
    private List<BackendTipoDocumento> documentos;
    private BackendDomicilio domicilio;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackendTipoDocumento {
        private Integer documentoId;
        private String nombre;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackendDomicilio {
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
}
