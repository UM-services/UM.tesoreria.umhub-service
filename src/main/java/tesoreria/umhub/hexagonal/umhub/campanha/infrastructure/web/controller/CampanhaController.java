package tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tesoreria.umhub.hexagonal.umhub.campanha.application.service.CampanhaService;
import tesoreria.umhub.hexagonal.umhub.campanha.domain.model.Campanha;
import tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.dto.CampanhaRequest;
import tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.dto.CampanhaWrappedResponse;
import tesoreria.umhub.hexagonal.umhub.campanha.infrastructure.web.mapper.CampanhaDtoMapper;

import java.util.UUID;

@RestController
@RequestMapping("/api/tesoreria/umhub/campanha")
@RequiredArgsConstructor
public class CampanhaController {

    private final CampanhaService campanhaService;
    private final CampanhaDtoMapper dtoMapper;

    @PostMapping("/")
    public ResponseEntity<CampanhaWrappedResponse> create(@RequestBody CampanhaRequest request) {
        Campanha domain = dtoMapper.toDomain(request);
        Campanha created = campanhaService.createCampanha(domain);
        CampanhaWrappedResponse response = dtoMapper.toWrappedResponse(created, "Campaña creada exitosamente");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CampanhaWrappedResponse> update(@PathVariable UUID id, @RequestBody CampanhaRequest request) {
        Campanha domain = dtoMapper.toDomain(request);
        Campanha updated = campanhaService.updateCampanha(id, domain);
        if (updated == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        CampanhaWrappedResponse response = dtoMapper.toWrappedResponse(updated, "Campaña modificada exitosamente");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
