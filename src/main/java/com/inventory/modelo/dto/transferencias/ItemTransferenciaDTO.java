package com.inventory.modelo.dto.transferencias;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO que representa un producto individual y su cantidad dentro de una transferencia.
 */
public record ItemTransferenciaDTO(
        @NotNull Long idProducto,
        @NotNull @DecimalMin("0.01") BigDecimal cantidad
) {}
