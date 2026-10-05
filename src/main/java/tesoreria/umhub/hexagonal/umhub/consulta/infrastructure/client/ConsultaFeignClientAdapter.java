package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import feign.FeignException;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaChequera;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DomicilioConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.TipoDocumentoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.VencimientoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.ports.out.ConsultaExternalService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador Feign hacia tesoreria-core (/umhub/consulta). El 404 del backend
 * (persona inexistente) se traduce a Optional.empty(); otros errores Feign se
 * propagan para que el controller responda 502.
 * Caché Caffeine (TTL 60 s, max 500 por tipo de consulta) solo para resultados
 * positivos: amortigua consultas repetidas y el costo de recalcular deuda.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultaFeignClientAdapter implements ConsultaExternalService {

    private static final Duration TTL = Duration.ofSeconds(60);
    private static final long MAXIMO = 500;

    private final ConsultaFeignClient feignClient;

    private final Cache<String, PersonaConsulta> personaCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL).maximumSize(MAXIMO).build();
    private final Cache<String, DeudaConsulta> deudaCache = Caffeine.newBuilder()
            .expireAfterWrite(TTL).maximumSize(MAXIMO).build();

    @Override
    public Optional<PersonaConsulta> getPersona(String numeroDocumento) {
        PersonaConsulta cached = personaCache.getIfPresent(numeroDocumento);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            BackendConsultaPersonaResponse response = feignClient.getPersona(numeroDocumento);
            PersonaConsulta domain = toPersonaDomain(response);
            if (domain != null) {
                personaCache.put(numeroDocumento, domain);
            }
            return Optional.ofNullable(domain);
        } catch (FeignException.NotFound e) {
            log.debug("Consulta de persona {} no encontrada en el backend", numeroDocumento);
            return Optional.empty();
        }
    }

    @Override
    public Optional<DeudaConsulta> getDeuda(String numeroDocumento, boolean extended) {
        String cacheKey = numeroDocumento + (extended ? ":ext" : ":base");
        DeudaConsulta cached = deudaCache.getIfPresent(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }
        try {
            BackendConsultaDeudaResponse response = feignClient.getDeuda(numeroDocumento, extended);
            DeudaConsulta domain = toDeudaDomain(response);
            if (domain != null) {
                deudaCache.put(cacheKey, domain);
            }
            return Optional.ofNullable(domain);
        } catch (FeignException.NotFound e) {
            log.debug("Consulta de deuda {} no encontrada en el backend", numeroDocumento);
            return Optional.empty();
        }
    }

    private PersonaConsulta toPersonaDomain(BackendConsultaPersonaResponse response) {
        if (response == null) {
            return null;
        }
        return PersonaConsulta.builder()
                .numeroDocumento(response.getNumeroDocumento())
                .nombre(response.getNombre())
                .apellido(response.getApellido())
                .sexo(response.getSexo())
                .numeroPrefijo(response.getNumeroPrefijo())
                .numeroPosfijo(response.getNumeroPosfijo())
                .documentos(response.getDocumentos() == null ? List.of()
                        : response.getDocumentos().stream()
                                .map(tipo -> TipoDocumentoConsulta.builder()
                                        .documentoId(tipo.getDocumentoId())
                                        .nombre(tipo.getNombre())
                                        .build())
                                .toList())
                .domicilio(toDomicilioDomain(response.getDomicilio()))
                .build();
    }

    private DomicilioConsulta toDomicilioDomain(BackendConsultaPersonaResponse.BackendDomicilio domicilio) {
        if (domicilio == null) {
            return null;
        }
        return DomicilioConsulta.builder()
                .calle(domicilio.getCalle())
                .puerta(domicilio.getPuerta())
                .piso(domicilio.getPiso())
                .dpto(domicilio.getDpto())
                .codigoPostal(domicilio.getCodigoPostal())
                .provinciaId(domicilio.getProvinciaId())
                .provinciaNombre(domicilio.getProvinciaNombre())
                .localidadId(domicilio.getLocalidadId())
                .localidadNombre(domicilio.getLocalidadNombre())
                .telefono(domicilio.getTelefono())
                .movil(domicilio.getMovil())
                .emailPersonal(domicilio.getEmailPersonal())
                .emailInstitucional(domicilio.getEmailInstitucional())
                .build();
    }

    private DeudaConsulta toDeudaDomain(BackendConsultaDeudaResponse response) {
        if (response == null) {
            return null;
        }
        List<DeudaChequera> deudas = response.getDeudas() == null ? List.of()
                : response.getDeudas().stream()
                        .map(chequera -> DeudaChequera.builder()
                                .facultadId(chequera.getFacultadId())
                                .facultad(chequera.getFacultad())
                                .tipoChequeraId(chequera.getTipochequeraId())
                                .tipoChequera(chequera.getTipochequera())
                                .chequeraSerieId(chequera.getChequeraserieId())
                                .lectivoId(chequera.getLectivoId())
                                .lectivo(chequera.getLectivo())
                                .alternativaId(chequera.getAlternativaId())
                                .total(chequera.getTotal())
                                .deuda(chequera.getDeuda())
                                .cuotas(chequera.getCuotas())
                                .vencimiento(chequera.getVencimiento1())
                                .importe(chequera.getImporte1())
                                .build())
                        .toList();
        List<VencimientoConsulta> vencimientos = response.getVencimientos() == null ? List.of()
                : response.getVencimientos().stream()
                        .map(vencimiento -> VencimientoConsulta.builder()
                                .producto(vencimiento.getProducto())
                                .periodo(vencimiento.getPeriodo())
                                .vencimiento(vencimiento.getVencimiento())
                                .importe(vencimiento.getImporte())
                                .initPoint(vencimiento.getInitPoint())
                                .build())
                        .toList();
        return DeudaConsulta.builder()
                .numeroDocumento(response.getNumeroDocumento())
                .cuotas(response.getCuotas())
                .deudaTotal(response.getDeuda())
                .deudas(deudas)
                .vencimientos(vencimientos)
                .build();
    }
}
