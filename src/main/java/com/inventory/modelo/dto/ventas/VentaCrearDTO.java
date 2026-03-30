package com.inventory.modelo.dto.ventas;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VentaCrearDTO(
        @NotNull Long idSucursal,
        Long idUsuarioVendedor,
        @NotNull @DecimalMin("0.00") BigDecimal totalVenta
) {
}


