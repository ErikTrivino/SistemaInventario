package com.inventory.servicios.implementaciones.tablero;

import com.inventory.servicios.interfaces.tablero.TableroServicio;
import com.inventory.modelo.dto.tablero.TableroResumenDTO;
import com.inventory.modelo.dto.tablero.AlertaStockDTO;
import com.inventory.repositorios.ventas.VentaRepositorio;
import com.inventory.repositorios.inventario.InventarioRepositorio;
import com.inventory.repositorios.transferencias.TransferenciaRepositorio;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableroServicioImpl implements TableroServicio {
    private final VentaRepositorio ventaRepositorio;
    private final InventarioRepositorio inventarioRepositorio;
    private final TransferenciaRepositorio transferenciaRepositorio;

    /**
     * RF-24: Dashboard crítico — agrega KPIs en tiempo real del día actual.
     * Incluye: ventas del día, alertas de stock mínimo y transferencias activas.
     */
    @Override
    public TableroResumenDTO getResumenDiario() {
        long ventasHoy = ventaRepositorio.countVentasHoy();
        BigDecimal ingresoHoy = ventaRepositorio.sumIngresoHoy();

        // Stock bajo mínimo y agotado
        List<AlertaStockDTO> alertas = getListaAlertasStock();
        long enStockMinimo = alertas.stream().filter(a -> a.stockActual().compareTo(BigDecimal.ZERO) > 0).count();
        long agotados = alertas.stream().filter(a -> a.stockActual().compareTo(BigDecimal.ZERO) == 0).count();

        // Transferencias activas
        org.springframework.data.domain.Pageable unpaged = org.springframework.data.domain.Pageable.unpaged();
        long pendientes = transferenciaRepositorio.findHistoricalTransfers(null, "SOLICITADO", null, null, unpaged).getContent().size();
        long enTransito = transferenciaRepositorio.findHistoricalTransfers(null, "EN_TRANSITO", null, null, unpaged).getContent().size();

        return new TableroResumenDTO(
                ventasHoy, ingresoHoy,
                enStockMinimo, agotados,
                pendientes, enTransito,
                0L, // ordenes pendientes recepción (módulo compras - extensible)
                alertas
        );
    }

    /**
     * RF-33: Devuelve los productos con stock por debajo del mínimo configurado (paginado).
     */
    @Override
    public org.springframework.data.domain.Page<AlertaStockDTO> getAlertasStock(Integer pagina, Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(numPagina, tamanoPagina);
        
        return inventarioRepositorio.findByQuantityLessThanMinStock(pageable)
                .map(inv -> new AlertaStockDTO(
                        inv.getProductoId(),
                        "Producto #" + inv.getProductoId(),
                        inv.getSucursalId(),
                        inv.getStock(),
                        inv.getStockMinimo(),
                        inv.getStockMinimo().subtract(inv.getStock())
                ));
    }

    /** RF-24: Métricas de transferencias activas (SOLICITADO + EN_TRANSITO). */
    @Override
    public Object getMetricasTransferencias() {
        org.springframework.data.domain.Pageable unpaged = org.springframework.data.domain.Pageable.unpaged();
        var pendientes = transferenciaRepositorio.findHistoricalTransfers(null, "SOLICITADO", null, null, unpaged).getContent();
        var enTransito = transferenciaRepositorio.findHistoricalTransfers(null, "EN_TRANSITO", null, null, unpaged).getContent();
        return java.util.Map.of(
                "pendientes", pendientes.size(),
                "enTransito", enTransito.size(),
                "total", pendientes.size() + enTransito.size()
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private List<AlertaStockDTO> getListaAlertasStock() {
        return inventarioRepositorio.findAll().stream()
                .filter(inv -> inv.getStock().compareTo(inv.getStockMinimo()) <= 0)
                .map(inv -> new AlertaStockDTO(
                        inv.getProductoId(),
                        "Producto #" + inv.getProductoId(), // nombre lookup simplificado
                        inv.getSucursalId(),
                        inv.getStock(),
                        inv.getStockMinimo(),
                        inv.getStockMinimo().subtract(inv.getStock())
                ))
                .collect(Collectors.toList());
    }
}



