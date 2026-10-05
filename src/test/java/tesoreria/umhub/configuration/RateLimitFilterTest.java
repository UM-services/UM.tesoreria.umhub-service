package tesoreria.umhub.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        ReflectionTestUtils.setField(filter, "enabled", true);
        ReflectionTestUtils.setField(filter, "requestsPerMinute", 3);
    }

    private MockHttpServletRequest consultaPersona() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tesoreria/umhub/persona/30123456");
        request.setRemoteAddr("10.0.0.1");
        return request;
    }

    private int doFilter(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void dejaPasarHastaElLimiteYRechazaElExcedente() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertThat(doFilter(consultaPersona())).isEqualTo(200);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(consultaPersona(), response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("Limite de solicitudes excedido");
        assertThat(response.getHeader("Retry-After")).isNotNull();
    }

    @Test
    void noAplicaFueraDeLasConsultas() throws Exception {
        MockHttpServletRequest reserva = new MockHttpServletRequest("GET", "/api/tesoreria/umhub/reservaVacante/reserva/status/1");
        reserva.setRemoteAddr("10.0.0.1");

        for (int i = 0; i < 10; i++) {
            assertThat(doFilter(reserva)).isEqualTo(200);
        }
    }

    @Test
    void deshabilitadoNuncaRechaza() throws Exception {
        ReflectionTestUtils.setField(filter, "enabled", false);
        ReflectionTestUtils.setField(filter, "requestsPerMinute", 0);

        for (int i = 0; i < 5; i++) {
            assertThat(doFilter(consultaPersona())).isEqualTo(200);
        }
    }

    @Test
    void separaClientesPorIp() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertThat(doFilter(consultaPersona())).isEqualTo(200);
        }

        MockHttpServletRequest otroCliente = consultaPersona();
        otroCliente.setRemoteAddr("10.0.0.2");

        assertThat(doFilter(otroCliente)).isEqualTo(200);
    }
}
