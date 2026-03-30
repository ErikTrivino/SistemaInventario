package com.inventory.modelo.dto.transferencias;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record TransferenciaRecepcionParcialDTO(
        @NotNull List<ItemRecibidoDTO> items
) {
    public record ItemRecibidoDTO(
            @NotNull Long idProducto,
            @NotNull @DecimalMin("0.00") BigDecimal cantidadRecibida
    ) {
    }
}


