package com.inventory.modelo.dto.transferencias;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaInformacionDTO(
        Long idTransferencia,
        Long idSucursalOrigen,
        Long idSucursalDestino,
        Long idProducto,
        BigDecimal cantidad,
        String estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaEnvioEstimada,
        LocalDateTime fechaRecepcionReal
) {
}


