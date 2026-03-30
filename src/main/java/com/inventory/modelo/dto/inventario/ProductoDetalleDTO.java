package com.inventory.modelo.dto.inventario;

import java.math.BigDecimal;

public record ProductoDetalleDTO(
        Long idProducto,
        String nombre,
        String descripcion,
        String sku,
        String unidadMedidaBase,
        BigDecimal precioCostoPromedio
) {
}


