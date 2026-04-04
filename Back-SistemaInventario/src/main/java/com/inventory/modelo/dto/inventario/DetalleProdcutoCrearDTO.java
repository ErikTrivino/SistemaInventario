package com.inventory.modelo.dto.inventario;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record DetalleProdcutoCrearDTO(

        @DecimalMin("0.00") BigDecimal precioCostoPromedio,
        @DecimalMin("0.00") BigDecimal cantidadInicial,
        @DecimalMin("0.00") BigDecimal cantidadMinima,
        Long idSucursal
) {
}
