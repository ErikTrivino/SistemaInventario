package com.inventory.modelo.dto.compras;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraInformacionDTO(
        Long idCompra,
        Long idSucursalDestino,
        Long idProveedor,
        Long idUsuarioResponsable,
        LocalDateTime fechaCompra,
        String estado,
        BigDecimal total
) {
}


