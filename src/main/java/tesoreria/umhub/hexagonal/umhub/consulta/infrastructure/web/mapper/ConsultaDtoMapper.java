package tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaChequera;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DeudaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.DomicilioConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.PersonaConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.TipoDocumentoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.domain.model.VencimientoConsulta;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto.ConsultaDeudaWrappedResponse;
import tesoreria.umhub.hexagonal.umhub.consulta.infrastructure.web.dto.ConsultaPersonaWrappedResponse;

@Component
public class ConsultaDtoMapper {

    public ConsultaPersonaWrappedResponse toPersonaResponse(PersonaConsulta domain, String mensaje) {
        return ConsultaPersonaWrappedResponse.builder()
                .success(true)
                .data(toPersonaData(domain))
                .mensaje(mensaje)
                .build();
    }

    public ConsultaPersonaWrappedResponse toPersonaNotFound() {
        return ConsultaPersonaWrappedResponse.builder()
                .success(false)
                .mensaje("Persona no encontrada")
                .build();
    }

    public ConsultaPersonaWrappedResponse toPersonaInvalidRequest() {
        return ConsultaPersonaWrappedResponse.builder()
                .success(false)
                .mensaje("numero_documento invalido: se requieren de 6 a 10 digitos")
                .build();
    }

    public ConsultaPersonaWrappedResponse toPersonaBackendUnavailable() {
        return ConsultaPersonaWrappedResponse.builder()
                .success(false)
                .mensaje("El servicio de consulta no esta disponible en este momento")
                .build();
    }

    public ConsultaDeudaWrappedResponse toDeudaResponse(DeudaConsulta domain, String mensaje) {
        return ConsultaDeudaWrappedResponse.builder()
                .success(true)
                .data(toDeudaData(domain))
                .mensaje(mensaje)
                .build();
    }

    public ConsultaDeudaWrappedResponse toDeudaNotFound() {
        return ConsultaDeudaWrappedResponse.builder()
                .success(false)
                .mensaje("Persona no encontrada")
                .build();
    }

    public ConsultaDeudaWrappedResponse toDeudaInvalidRequest() {
        return ConsultaDeudaWrappedResponse.builder()
                .success(false)
                .mensaje("numero_documento invalido: se requieren de 6 a 10 digitos")
                .build();
    }

    public ConsultaDeudaWrappedResponse toDeudaBackendUnavailable() {
        return ConsultaDeudaWrappedResponse.builder()
                .success(false)
                .mensaje("El servicio de consulta no esta disponible en este momento")
                .build();
    }

    private ConsultaPersonaWrappedResponse.PersonaData toPersonaData(PersonaConsulta domain) {
        if (domain == null) {
            return null;
        }
        return ConsultaPersonaWrappedResponse.PersonaData.builder()
                .numeroDocumento(domain.getNumeroDocumento())
                .nombre(domain.getNombre())
                .apellido(domain.getApellido())
                .sexo(domain.getSexo())
                .numeroPrefijo(domain.getNumeroPrefijo())
                .numeroPosfijo(domain.getNumeroPosfijo())
                .documentos(domain.getDocumentos() == null ? null
                        : domain.getDocumentos().stream().map(this::toTipoDocumentoData).toList())
                .contacto(toContactoData(domain.getDomicilio()))
                .domicilio(toDomicilioData(domain.getDomicilio()))
                .build();
    }

    private ConsultaPersonaWrappedResponse.TipoDocumentoData toTipoDocumentoData(TipoDocumentoConsulta domain) {
        return ConsultaPersonaWrappedResponse.TipoDocumentoData.builder()
                .tipoDocumentoId(domain.getDocumentoId())
                .tipoDocumento(domain.getNombre())
                .build();
    }

    private ConsultaPersonaWrappedResponse.ContactoData toContactoData(DomicilioConsulta domain) {
        if (domain == null) {
            return null;
        }
        return ConsultaPersonaWrappedResponse.ContactoData.builder()
                .emailPersonal(domain.getEmailPersonal())
                .emailInstitucional(domain.getEmailInstitucional())
                .telefono(domain.getTelefono())
                .movil(domain.getMovil())
                .build();
    }

    private ConsultaPersonaWrappedResponse.DomicilioData toDomicilioData(DomicilioConsulta domain) {
        if (domain == null) {
            return null;
        }
        return ConsultaPersonaWrappedResponse.DomicilioData.builder()
                .calle(domain.getCalle())
                .puerta(domain.getPuerta())
                .piso(domain.getPiso())
                .dpto(domain.getDpto())
                .codigoPostal(domain.getCodigoPostal())
                .provinciaId(domain.getProvinciaId())
                .provinciaNombre(domain.getProvinciaNombre())
                .localidadId(domain.getLocalidadId())
                .localidadNombre(domain.getLocalidadNombre())
                .build();
    }

    private ConsultaDeudaWrappedResponse.DeudaData toDeudaData(DeudaConsulta domain) {
        if (domain == null) {
            return null;
        }
        return ConsultaDeudaWrappedResponse.DeudaData.builder()
                .numeroDocumento(domain.getNumeroDocumento())
                .cuotas(domain.getCuotas())
                .deudaTotal(domain.getDeudaTotal())
                .deudas(domain.getDeudas() == null ? null
                        : domain.getDeudas().stream().map(this::toDeudaChequeraData).toList())
                .vencimientos(domain.getVencimientos() == null ? null
                        : domain.getVencimientos().stream().map(this::toVencimientoData).toList())
                .build();
    }

    private ConsultaDeudaWrappedResponse.DeudaChequeraData toDeudaChequeraData(DeudaChequera domain) {
        return ConsultaDeudaWrappedResponse.DeudaChequeraData.builder()
                .facultadId(domain.getFacultadId())
                .facultad(domain.getFacultad())
                .tipoChequeraId(domain.getTipoChequeraId())
                .tipoChequera(domain.getTipoChequera())
                .chequeraSerieId(domain.getChequeraSerieId())
                .lectivoId(domain.getLectivoId())
                .lectivo(domain.getLectivo())
                .alternativaId(domain.getAlternativaId())
                .total(domain.getTotal())
                .deuda(domain.getDeuda())
                .cuotas(domain.getCuotas())
                .vencimiento(domain.getVencimiento())
                .importe(domain.getImporte())
                .build();
    }

    private ConsultaDeudaWrappedResponse.VencimientoData toVencimientoData(VencimientoConsulta domain) {
        return ConsultaDeudaWrappedResponse.VencimientoData.builder()
                .producto(domain.getProducto())
                .periodo(domain.getPeriodo())
                .vencimiento(domain.getVencimiento())
                .importe(domain.getImporte())
                .initPoint(domain.getInitPoint())
                .build();
    }
}
