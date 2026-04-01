package com.inventory.modelo.dto.reportes;

import java.util.Date;
import org.springframework.data.domain.Page;

/** RF-29/RF-30: Reporte de transferencias entre sucursales. */
public record ReporteTransferenciasDTO(
        Date fechaInicio,
        Date fechaFin,
        long totalTransferencias,
        long completadas,
        long conDiscrepancias,
        long pendientes,
        Page<ItemTransferenciaDTO> detalle
) {}
