package com.inventory.modelo.dto.inventario;

import java.math.BigDecimal;

public record InventarioInformacionDTO(
        Long idSucursal,
        Long idProducto,
        BigDecimal stockActual,
        BigDecimal stockMinimo
) {
}


