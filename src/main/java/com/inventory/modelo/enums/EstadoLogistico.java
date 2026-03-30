package com.inventory.modelo.enums;

/**
 * Enum para los estados logísticos del envío de una transferencia.
 */
public enum EstadoLogistico {
    /** Mercancía en preparación para despacho. */
    PREPARACION,
    /** Mercancía despachada y en camino al destino. */
    EN_TRANSITO,
    /** Mercancía recibida satisfactoriamente. */
    RECIBIDO,
    /** Mercancía recibida con diferencias o daños. */
    CON_FALTANTES
}
