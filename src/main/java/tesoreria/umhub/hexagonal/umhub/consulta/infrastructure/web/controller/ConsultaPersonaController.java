package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tesoreria.umhub.hexagonal.umhub.consulta.application.service.ConsultaService;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto.ConsultaDeudaWrappedResponse;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto.ConsultaPersonaWrappedResponse;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.mapper.ConsultaDtoMapper;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Endpoints publicos del hub para consultar identidad/contacto y deuda por
 * numero de documento (sin tipo). Protegidos por ApiKeyFilter (X-API-Key).
 */
@RestController
@RequestMapping("/api/tesoreria/umhub/persona")
@RequiredArgsConstructor
@Slf4j
public class ConsultaPersonaController {

    private static final Pattern NUMERO_DOCUMENTO = Pattern.compile("\\d{6,10}");

    private final ConsultaService consultaService;
    private final ConsultaDtoMapper dtoMapper;

    @GetMapping("/{numeroDocumento}")
    public ResponseEntity<ConsultaPersonaWrappedResponse> getPersona(@PathVariable String numeroDocumento) {
        if (!NUMERO_DOCUMENTO.matcher(numeroDocumento).matches()) {
            return ResponseEntity.badRequest().body(dtoMapper.toPersonaInvalidRequest());
        }
        try {
            Optional<PersonaConsulta> domain = consultaService.findPersona(numeroDocumento);
            if (domain.isEmpty()) {
                return ResponseEntity.status(404).body(dtoMapper.toPersonaNotFound());
            }
            return ResponseEntity.ok(dtoMapper.toPersonaResponse(domain.get(), "Persona encontrada"));
        } catch (FeignException e) {
            log.error("Fallo del backend de consulta para persona {} (status {})", numeroDocumento, e.status());
            return ResponseEntity.status(502).body(dtoMapper.toPersonaBackendUnavailable());
        }
    }

    @GetMapping("/{numeroDocumento}/deuda")
    public ResponseEntity<ConsultaDeudaWrappedResponse> getDeuda(@PathVariable String numeroDocumento,
            @RequestParam(name = "extended", defaultValue = "false") boolean extended) {
        if (!NUMERO_DOCUMENTO.matcher(numeroDocumento).matches()) {
            return ResponseEntity.badRequest().body(dtoMapper.toDeudaInvalidRequest());
        }
        try {
            Optional<DeudaConsulta> domain = consultaService.findDeuda(numeroDocumento, extended);
            if (domain.isEmpty()) {
                return ResponseEntity.status(404).body(dtoMapper.toDeudaNotFound());
            }
            return ResponseEntity.ok(dtoMapper.toDeudaResponse(domain.get(), "Deuda consultada"));
        } catch (FeignException e) {
            log.error("Fallo del backend de consulta para deuda {} (status {})", numeroDocumento, e.status());
            return ResponseEntity.status(502).body(dtoMapper.toDeudaBackendUnavailable());
        }
    }
}
