package com.inventory.eventos;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Evento que se dispara cuando se procesa y completa una venta.
 */
@Getter
public class VentaCompletadaEvento extends ApplicationEvent {

    private final Long ventaId;
    private final Long sucursalId;
    private final Long vendedorId;
    private final BigDecimal total;
    private final Date fechaVenta;
    /** ID del usuario (vendedor) que registró la venta. */
    private final String usuarioResponsable;

    public VentaCompletadaEvento(Object source,
                                 Long ventaId,
                                 Long sucursalId,
                                 Long vendedorId,
                                 BigDecimal total,
                                 Date fechaVenta,
                                 String usuarioResponsable) {
        super(source);
        this.ventaId            = ventaId;
        this.sucursalId         = sucursalId;
        this.vendedorId         = vendedorId;
        this.total              = total;
        this.fechaVenta         = fechaVenta;
        this.usuarioResponsable = usuarioResponsable;
    }
}
