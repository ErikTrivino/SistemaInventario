package com.inventory.modelo.dto.inventario;

import java.math.BigDecimal;

public record InventarioRespuestaDTO(
        Long idProducto,
        String nombreProducto,
        String sku,
        String unidadMedida,
        String descripcion,
        Boolean activo,
        Long idSucursal,
        BigDecimal stock,
        BigDecimal stockMinimo,
        BigDecimal precioCostoPromedio,
        Long idProveedor
) {}
