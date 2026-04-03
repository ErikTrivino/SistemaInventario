package com.inventory.modelo.dto.ventas;

import java.math.BigDecimal;
import java.util.Date;

public record VentaInformacionDTO(
                Long idVenta,
                Long idSucursal,
                Long idUsuarioVendedor,
                Date fechaVenta,
                BigDecimal total,
                String comprobanteOriginal) {
}
