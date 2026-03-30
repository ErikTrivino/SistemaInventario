package com.inventory.modelo.dto.inventario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoEditarDTO(
        @NotBlank @Size(max = 150) String nombre,
        String descripcion,
        @Size(max = 50) String sku,
        @NotBlank @Size(max = 20) String unidadMedidaBase,
        @DecimalMin("0.00") BigDecimal precioCostoPromedio
) {
}


