package com.inventory.servicios.interfaces.reportes;

import com.inventory.modelo.dto.reportes.*;
import java.util.Date;

public interface ReporteServicio {
    /** RF-29/RF-30: Reporte de ventas por período con desglose por sucursal. */
    ReporteVentasDTO generarReporteVentas(Date inicio, Date fin, Integer pagina, Integer porPagina);

    /** RF-29/RF-30: Reporte de inventario de una sucursal en un momento. */
    ReporteInventarioDTO generarReporteInventario(Long idSucursal, Integer pagina, Integer porPagina);

    /** RF-29/RF-30: Reporte de transferencias por período. */
    ReporteTransferenciasDTO generarReporteTransferencias(Date inicio, Date fin, Integer pagina, Integer porPagina);

    /** RF-31: Comparativo de ventas mensual por año. */
    ReporteComparativoDTO generarComparativoAnual(int anio);

    /** RF-32: Análisis de rotación ABC de productos por mes y año. */
    ReporteRotacionDTO generarAnalisisRotacion(int mes, int anio, Integer pagina, Integer porPagina);
}



