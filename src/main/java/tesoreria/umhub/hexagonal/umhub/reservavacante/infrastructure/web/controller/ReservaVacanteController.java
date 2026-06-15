package tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tesoreria.umhub.hexagonal.umhub.reservavacante.application.service.ReservaVacanteService;
import tesoreria.umhub.hexagonal.umhub.reservavacante.domain.model.ReservaVacante;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.dto.ReservaVacanteRequest;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.dto.ReservaVacanteWrappedResponse;
import tesoreria.umhub.hexagonal.umhub.reservavacante.infrastructure.web.mapper.ReservaVacanteDtoMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/tesoreria/umhub/reservaVacante")
@RequiredArgsConstructor
public class ReservaVacanteController {

    private final ReservaVacanteService reservaVacanteService;
    private final ReservaVacanteDtoMapper dtoMapper;

    @PostMapping("/vacante/add")
    public ResponseEntity<ReservaVacanteWrappedResponse> create(@RequestBody ReservaVacanteRequest request) {
        ReservaVacante domain = dtoMapper.toDomain(request);
        ReservaVacante created = reservaVacanteService.createReservaVacante(domain);
        ReservaVacanteWrappedResponse response = dtoMapper.toWrappedResponse(created, "Reserva creada exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reserva/status/{id}")
    public ResponseEntity<ReservaVacanteWrappedResponse> getStatus(@PathVariable UUID id) {
        ReservaVacante domain = reservaVacanteService.getStatus(id);
        if (domain == null) {
            return ResponseEntity.notFound().build();
        }
        ReservaVacanteWrappedResponse response = dtoMapper.toWrappedResponse(domain, "Estado consultado exitosamente");
        return ResponseEntity.ok(response);
    }
}
