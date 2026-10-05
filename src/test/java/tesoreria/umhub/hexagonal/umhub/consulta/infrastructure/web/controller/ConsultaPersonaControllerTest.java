package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.controller;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tesoreria.umhub.hexagonal.umhub.consulta.application.service.ConsultaService;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DomicilioConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.TipoDocumentoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.mapper.ConsultaDtoMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ConsultaPersonaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ConsultaDtoMapper.class)
@TestPropertySource(properties = "app.api-key=test-key")
class ConsultaPersonaControllerTest {

    private static final String URL = "/api/tesoreria/umhub/persona/30123456";

    @Autowired
    private MockMvcTester mockMvc;
    @MockitoBean
    private ConsultaService consultaService;

    private PersonaConsulta persona() {
        return PersonaConsulta.builder()
                .numeroDocumento("30123456").nombre("Maria").apellido("GARCIA").sexo("F")
                .numeroPrefijo("015").numeroPosfijo("5551234")
                .documentos(List.of(TipoDocumentoConsulta.builder().documentoId(8).nombre("DNI").build()))
                .domicilio(DomicilioConsulta.builder()
                        .calle("San Martin").codigoPostal("M5500").provinciaNombre("Mendoza")
                        .telefono("02614000000").movil("02615000000").emailPersonal("maria@example.com")
                        .build())
                .build();
    }

    @Test
    void exponePersonaConContratoSnakeCase() {
        when(consultaService.findPersona("30123456")).thenReturn(Optional.of(persona()));

        var response = mockMvc.get().uri(URL).assertThat().hasStatusOk();

        response.bodyJson().extractingPath("$.success").isEqualTo(true);
        response.bodyJson().extractingPath("$.data.numero_documento").isEqualTo("30123456");
        response.bodyJson().extractingPath("$.data.documentos[0].tipo_documento").isEqualTo("DNI");
        response.bodyJson().extractingPath("$.data.contacto.email_personal").isEqualTo("maria@example.com");
        response.bodyJson().extractingPath("$.data.contacto.movil").isEqualTo("02615000000");
        response.bodyJson().extractingPath("$.data.domicilio.provincia_nombre").isEqualTo("Mendoza");
    }

    @Test
    void responde404EnvueltoCuandoLaPersonaNoExiste() {
        when(consultaService.findPersona("30123456")).thenReturn(Optional.empty());

        var response = mockMvc.get().uri(URL).assertThat().hasStatus(404);

        response.bodyJson().extractingPath("$.success").isEqualTo(false);
    }

    @Test
    void rechazaNumerosInvalidosSinLlamarAlBackend() {
        mockMvc.get().uri("/api/tesoreria/umhub/persona/123").assertThat().hasStatus(400);

        verify(consultaService, never()).findPersona(any());
    }

    @Test
    void exponeDeudaAgregada() {
        when(consultaService.findDeuda("30123456", false)).thenReturn(Optional.of(
                DeudaConsulta.builder().numeroDocumento("30123456").cuotas(4)
                        .deudaTotal(new BigDecimal("458.25")).deudas(List.of()).build()));

        var response = mockMvc.get().uri(URL + "/deuda").assertThat().hasStatusOk();

        response.bodyJson().extractingPath("$.data.deuda_total").isEqualTo(458.25);
        response.bodyJson().extractingPath("$.data.cuotas").isEqualTo(4);
    }

    @Test
    void propagaElParametroExtended() {
        when(consultaService.findDeuda("30123456", true)).thenReturn(Optional.of(
                DeudaConsulta.builder().numeroDocumento("30123456").cuotas(0)
                        .deudaTotal(BigDecimal.ZERO).deudas(List.of()).build()));

        mockMvc.get().uri(URL + "/deuda?extended=true").assertThat().hasStatusOk();

        verify(consultaService).findDeuda("30123456", true);
        verify(consultaService, never()).findDeuda("30123456", false);
    }

    @Test
    void responde502CuandoElBackendFalla() {
        Request request = Request.create(Request.HttpMethod.GET, "http://core/consulta",
                java.util.Map.of(), (byte[]) null, null);
        Response feignResponse = Response.builder().status(503).reason("Unavailable")
                .request(request).headers(java.util.Map.of()).build();
        when(consultaService.findPersona(any())).thenThrow(FeignException.errorStatus("ConsultaFeignClient#getPersona", feignResponse));

        var response = mockMvc.get().uri(URL).assertThat().hasStatus(502);

        response.bodyJson().extractingPath("$.success").isEqualTo(false);
    }
}
