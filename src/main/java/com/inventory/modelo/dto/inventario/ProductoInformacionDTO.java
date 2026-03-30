package com.inventory.modelo.dto.inventario;

import java.math.BigDecimal;

public record ProductoInformacionDTO(
        Long idProducto,
        String nombre,
        String sku,
        String unidadMedidaBase,
        BigDecimal precioCostoPromedio
) {
}


