package com.inventory.modelo.dto.compras;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraHistoricoRespuestaDTO(
        Long idOrdenCompra,
        Long idDetalle,
        Long idProducto,
        String nombreProducto,
        Long idProveedor,
        String nombreProveedor,
        BigDecimal cantidadSolicitada,
        BigDecimal cantidadRecibida,
        BigDecimal precioUnitario,
        LocalDateTime fechaCompra
) {}
