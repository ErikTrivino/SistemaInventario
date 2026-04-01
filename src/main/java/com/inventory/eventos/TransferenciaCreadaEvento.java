package com.inventory.eventos;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento que se dispara cuando se crea o actualiza una transferencia entre sucursales.
 */
@Getter
public class TransferenciaCreadaEvento extends ApplicationEvent {

    private final Long transferenciaId;
    private final Long sucursalOrigenId;
    private final Long sucursalDestinoId;
    private final String estado;
    private final LocalDateTime fechaSolicitud;
    /** ID del usuario que solicitó o actualizó el estado de la transferencia. */
    private final String usuarioResponsable;

    public TransferenciaCreadaEvento(Object source,
                                     Long transferenciaId,
                                     Long sucursalOrigenId,
                                     Long sucursalDestinoId,
                                     String estado,
                                     LocalDateTime fechaSolicitud,
                                     String usuarioResponsable) {
        super(source);
        this.transferenciaId    = transferenciaId;
        this.sucursalOrigenId   = sucursalOrigenId;
        this.sucursalDestinoId  = sucursalDestinoId;
        this.estado             = estado;
        this.fechaSolicitud     = fechaSolicitud;
        this.usuarioResponsable = usuarioResponsable;
    }
}
