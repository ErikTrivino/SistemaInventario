package com.inventory.modelo.dto.reportes;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;

/** RF-29/RF-30: Reporte de inventario con productos y niveles de stock. */
public record ReporteInventarioDTO(
        Date fechaGeneracion,
        Long idSucursal,
        long totalProductos,
        long productosEnStockMinimo,
        long productosAgotados,
        BigDecimal valorTotalInventario,
        Page<ItemInventarioDTO> detalle
) {}
