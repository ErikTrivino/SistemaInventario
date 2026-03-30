package com.inventory.modelo.dto.compras;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CompraCrearDTO(
        Long idSucursalDestino,
        @NotNull Long idProveedor,
        Long idUsuarioResponsable,
        @DecimalMin("0.00") BigDecimal total
) {
}


