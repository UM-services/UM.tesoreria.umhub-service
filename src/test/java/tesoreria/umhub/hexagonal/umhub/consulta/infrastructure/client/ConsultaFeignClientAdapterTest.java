package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.client;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultaFeignClientAdapterTest {

    private final ConsultaFeignClient feignClient = mock(ConsultaFeignClient.class);
    private final ConsultaFeignClientAdapter adapter = new ConsultaFeignClientAdapter(feignClient);

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "http://core/consulta",
                Map.of(), (byte[]) null, null);
        Response response = Response.builder()
                .status(404).reason("Not Found").request(request).headers(Map.of()).build();
        return (FeignException.NotFound) FeignException.errorStatus("ConsultaFeignClient#getPersona", response);
    }

    private BackendConsultaPersonaResponse backendPersona() {
        return BackendConsultaPersonaResponse.builder()
                .numeroDocumento("30123456").nombre("Maria").apellido("GARCIA").sexo("F")
                .numeroPrefijo("015").numeroPosfijo("5551234")
                .documentos(List.of(BackendConsultaPersonaResponse.BackendTipoDocumento.builder()
                        .documentoId(8).nombre("DNI").build()))
                .domicilio(BackendConsultaPersonaResponse.BackendDomicilio.builder()
                        .calle("San Martin").emailPersonal("maria@example.com").movil("02615000000")
                        .provinciaNombre("Mendoza").build())
                .build();
    }

    @Test
    void mapeaBackendAPersonaConsulta() {
        when(feignClient.getPersona("30123456")).thenReturn(backendPersona());

        Optional<PersonaConsulta> result = adapter.getPersona("30123456");

        assertThat(result).isPresent();
        assertThat(result.get().getApellido()).isEqualTo("GARCIA");
        assertThat(result.get().getDomicilio().getEmailPersonal()).isEqualTo("maria@example.com");
        assertThat(result.get().getDocumentos()).singleElement()
                .satisfies(tipo -> assertThat(tipo.getNombre()).isEqualTo("DNI"));
    }

    @Test
    void traduce404DelBackendEnVacio() {
        when(feignClient.getPersona(anyString())).thenThrow(notFound());

        assertThat(adapter.getPersona("99999999")).isEmpty();
    }

    @Test
    void cacheaResultadoPositivoUnaVentana() {
        when(feignClient.getPersona("30123456")).thenReturn(backendPersona());

        adapter.getPersona("30123456");
        adapter.getPersona("30123456");

        verify(feignClient, times(1)).getPersona("30123456");
    }

    @Test
    void noCacheaResultadoNegativo() {
        when(feignClient.getPersona(anyString())).thenThrow(notFound());

        adapter.getPersona("99999999");
        adapter.getPersona("99999999");

        verify(feignClient, times(2)).getPersona("99999999");
    }

    @Test
    void mapeaDeudaYVencimientos() {
        BackendConsultaDeudaResponse backend = BackendConsultaDeudaResponse.builder()
                .numeroDocumento("30123456").cuotas(2).deuda(new BigDecimal("150.50"))
                .deudas(List.of(BackendConsultaDeudaResponse.BackendDeudaChequera.builder()
                        .facultad("Educacion").tipochequera("Posgrado").tipochequeraId(4)
                        .cuotas(2).deuda(new BigDecimal("150.50")).build()))
                .vencimientos(List.of(BackendConsultaDeudaResponse.BackendVencimiento.builder()
                        .producto("Arancel").initPoint("https://mp/link").build()))
                .build();
        when(feignClient.getDeuda("30123456", true)).thenReturn(backend);

        Optional<DeudaConsulta> result = adapter.getDeuda("30123456", true);

        assertThat(result).isPresent();
        assertThat(result.get().getDeudaTotal()).isEqualByComparingTo("150.50");
        assertThat(result.get().getDeudas()).singleElement()
                .satisfies(deuda -> assertThat(deuda.getTipoChequera()).isEqualTo("Posgrado"));
        assertThat(result.get().getVencimientos()).singleElement()
                .satisfies(vencimiento -> assertThat(vencimiento.getInitPoint()).isEqualTo("https://mp/link"));
    }

    @Test
    void propagaErroresDistintosDe404() {
        when(feignClient.getDeuda(anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> adapter.getDeuda("30123456", false))
                .isInstanceOf(RuntimeException.class);
    }
}
