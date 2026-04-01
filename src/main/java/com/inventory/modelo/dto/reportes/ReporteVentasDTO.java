package com.inventory.modelo.dto.reportes;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.data.domain.Page;

/** RF-29/RF-30: Reporte de ventas con desglose por sucursal y período. */
public record ReporteVentasDTO(
        Date fechaInicio,
        Date fechaFin,
        long totalVentas,
        BigDecimal ingresoTotal,
        BigDecimal promedioVenta,
        Page<ResumenVentaSucursalDTO> porSucursal
) {}
