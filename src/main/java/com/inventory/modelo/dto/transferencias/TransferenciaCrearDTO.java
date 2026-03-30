package com.inventory.modelo.dto.transferencias;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferenciaCrearDTO(
        @NotNull Long idSucursalOrigen,
        @NotNull Long idSucursalDestino,
        @NotNull Long idProducto,
        @NotNull @DecimalMin("0.01") BigDecimal cantidad,
        Long idUsuarioSolicita,
        LocalDateTime fechaEnvioEstimada
) {
}


