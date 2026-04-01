package com.inventory.servicios.interfaces.tablero;

import com.inventory.modelo.dto.tablero.TableroResumenDTO;

public interface TableroServicio {
    /** RF-24: Dashboard crítico con KPIs del día. */
    TableroResumenDTO getResumenDiario();

    /** RF-33: Lista de productos en alerta de stock mínimo. */
    Object getAlertasStock(Integer pagina, Integer porPagina);

    /** RF-24: Métricas de transferencias activas. */
    Object getMetricasTransferencias();
}



