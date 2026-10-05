package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaChequera;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DomicilioConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.TipoDocumentoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.VencimientoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto.ConsultaDeudaWrappedResponse;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto.ConsultaPersonaWrappedResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultaDtoMapperTest {

    private final ConsultaDtoMapper mapper = new ConsultaDtoMapper();

    private PersonaConsulta persona() {
        return PersonaConsulta.builder()
                .numeroDocumento("30123456").nombre("Maria").apellido("GARCIA").sexo("F")
                .numeroPrefijo("015").numeroPosfijo("5551234")
                .documentos(List.of(TipoDocumentoConsulta.builder().documentoId(8).nombre("DNI").build()))
                .domicilio(DomicilioConsulta.builder()
                        .calle("San Martin").puerta("1234").codigoPostal("M5500")
                        .provinciaId(13).provinciaNombre("Mendoza")
                        .localidadId(255).localidadNombre("Ciudad")
                        .telefono("02614000000").movil("02615000000")
                        .emailPersonal("maria@example.com").emailInstitucional("mgarcia@um.edu.ar")
                        .build())
                .build();
    }

    @Test
    void personaEnvuelveConSnakeCaseYSeparaContactoDeDomicilio() {
        ConsultaPersonaWrappedResponse response = mapper.toPersonaResponse(persona(), "Persona encontrada");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getNumeroDocumento()).isEqualTo("30123456");
        assertThat(response.getData().getContacto().getEmailPersonal()).isEqualTo("maria@example.com");
        assertThat(response.getData().getContacto().getMovil()).isEqualTo("02615000000");
        assertThat(response.getData().getDomicilio().getProvinciaNombre()).isEqualTo("Mendoza");
        assertThat(response.getData().getDocumentos()).singleElement()
                .satisfies(tipo -> assertThat(tipo.getTipoDocumento()).isEqualTo("DNI"));
    }

    @Test
    void serializacionJsonUsaLasClavesPublicadas() throws Exception {
        String json = JsonMapper.builder().build().writeValueAsString(mapper.toPersonaResponse(persona(), "ok"));

        assertThat(json)
                .contains("\"numero_documento\":\"30123456\"")
                .contains("\"email_personal\":\"maria@example.com\"")
                .contains("\"numero_prefijo\":\"015\"")
                .contains("\"tipo_documento_id\":8")
                .doesNotContain("emailPagador")
                .doesNotContain("password");
    }

    @Test
    void deudaExponeDeudaTotalYVencimientosConInitPoint() {
        DeudaConsulta domain = DeudaConsulta.builder()
                .numeroDocumento("30123456").cuotas(2).deudaTotal(new BigDecimal("150.50"))
                .deudas(List.of(DeudaChequera.builder()
                        .facultad("Educacion").tipoChequera("Posgrado")
                        .vencimiento(OffsetDateTime.of(2026, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                        .importe(new BigDecimal("150.50")).cuotas(2).build()))
                .vencimientos(List.of(VencimientoConsulta.builder()
                        .producto("Arancel").vencimiento("2026-09-01").initPoint("https://mp/link").build()))
                .build();

        ConsultaDeudaWrappedResponse response = mapper.toDeudaResponse(domain, "Deuda consultada");

        assertThat(response.getData().getDeudaTotal()).isEqualByComparingTo("150.50");
        assertThat(response.getData().getDeudas()).singleElement()
                .satisfies(deuda -> assertThat(deuda.getVencimiento().getYear()).isEqualTo(2026));
        assertThat(response.getData().getVencimientos()).singleElement()
                .satisfies(vencimiento -> assertThat(vencimiento.getInitPoint()).isEqualTo("https://mp/link"));
    }

    @Test
    void respuestasDeErrorLlevanSuccessFalse() {
        assertThat(mapper.toPersonaNotFound().isSuccess()).isFalse();
        assertThat(mapper.toPersonaNotFound().getMensaje()).isEqualTo("Persona no encontrada");
        assertThat(mapper.toDeudaInvalidRequest().getData()).isNull();
        assertThat(mapper.toPersonaBackendUnavailable().isSuccess()).isFalse();
    }
}
