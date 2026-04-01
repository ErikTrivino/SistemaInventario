package com.inventory.modelo.dto.reportes;

import org.springframework.data.domain.Page;

/**
 * RF-32: Análisis de rotación de productos (clasificación ABC).
 * A = alta rotación (top 20%), B = media, C = baja rotación.
 */
public record ReporteRotacionDTO(
        int anio,
        int mes,
        Page<ItemRotacionDTO> productos
) {}
