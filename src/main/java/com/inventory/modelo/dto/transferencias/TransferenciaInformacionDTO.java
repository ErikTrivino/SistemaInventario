package com.inventory.modelo.dto.transferencias;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO detallado para la consulta de una transferencia con sus productos.
 */
public record TransferenciaInformacionDTO(
        Long idTransferencia,
        Long idSucursalOrigen,
        Long idSucursalDestino,
        String estado,
        LocalDateTime fechaSolicitud,
        List<ResumenDetalleDTO> items,
        EnvioInfoDTO envio
) {
    /**
     * DTO interno para el resumen de cada producto en la transferencia.
     */
    public record ResumenDetalleDTO(
            Long idProducto,
            java.math.BigDecimal cantidadSolicitada,
            java.math.BigDecimal cantidadConfirmada,
            java.math.BigDecimal cantidadRecibida,
            String motivoDiferencia
    ) {}

    /**
     * DTO interno para información logística básica.
     */
    public record EnvioInfoDTO(
            Long idEnvio,
            LocalDateTime fechaDespacho,
            Integer tiempoEstimado,
            LocalDateTime fechaRecepcionReal,
            String estadoLogistico
    ) {}
}
