package com.inventory.modelo.dto.inventario;

import java.math.BigDecimal;

public record ProductoDetallePorSucursalDTO(
        Long id,
        String nombre,
        String descripcion,
        String sku,
        String unidadMedidaBase,
        BigDecimal precioCostoPromedio,
        BigDecimal stock,
        Long idSucursal,
        Long proveedor

) {
}
