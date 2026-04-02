package com.inventory.modelo.dto.inventario;


public record ProductoInformacionDTO(
        Long id,
        String nombre,
        String descripcion,
        String sku,
        String unidadMedidaBase,
        java.math.BigDecimal precioCostoPromedio,
        boolean activo
) {
}


