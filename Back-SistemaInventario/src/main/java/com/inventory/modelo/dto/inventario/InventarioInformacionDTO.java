package com.inventory.modelo.dto.inventario;

import java.math.BigDecimal;

public record InventarioInformacionDTO(
        Long idSucursal,
        Long idProducto,
        String nombreProducto,
        String sku,
        String descripcion,
        BigDecimal stockActual,
        BigDecimal stockMinimo
) {
}


