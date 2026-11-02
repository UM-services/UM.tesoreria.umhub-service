package tesoreria.umhub.hexagonal.umhub.reservavacante.domain.ports.out;

import java.util.UUID;

/**
 * Claim de la notificación de pago. El estado vive en {@code tesoreria-core-service} (dueño de
 * {@code reserva_vacante}): umhub no tiene base de datos.
 */
public interface ReservaVacanteNotificacionClaimPort {

    /**
     * @return {@code true} si esta entrega ganó el claim y debe notificar; {@code false} si otra
     * entrega del mismo evento ya notificó.
     */
    boolean claim(UUID reservaVacanteId);

    /**
     * El envío falló: libera la marca para que el reintento de Kafka pueda volver a reclamarla.
     */
    void release(UUID reservaVacanteId);

}
