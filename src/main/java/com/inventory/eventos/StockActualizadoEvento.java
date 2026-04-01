package com.inventory.eventos;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Evento que se dispara cuando el stock de un producto en una sucursal cambia.
 * Se usa principalmente para detectar alertas de bajo stock.
 */
@Getter
public class StockActualizadoEvento extends ApplicationEvent {

    private final Long productoId;
    private final String nombreProducto;
    private final Long sucursalId;
    private final BigDecimal stockActual;
    private final BigDecimal stockMinimo;
    /** ID del usuario que ejecutó la operación que causó el cambio de stock. */
    private final String usuarioResponsable;

    public StockActualizadoEvento(Object source,
                                  Long productoId,
                                  String nombreProducto,
                                  Long sucursalId,
                                  BigDecimal stockActual,
                                  BigDecimal stockMinimo,
                                  String usuarioResponsable) {
        super(source);
        this.productoId         = productoId;
        this.nombreProducto     = nombreProducto;
        this.sucursalId         = sucursalId;
        this.stockActual        = stockActual;
        this.stockMinimo        = stockMinimo;
        this.usuarioResponsable = usuarioResponsable;
    }
}
