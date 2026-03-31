package com.inventory.servicios.implementaciones.reportes;

import com.inventory.servicios.interfaces.reportes.ReporteServicio;
import com.inventory.modelo.dto.reportes.*;
import com.inventory.repositorios.ventas.VentaRepositorio;
import com.inventory.repositorios.ventas.DetalleVentaRepositorio;
import com.inventory.repositorios.inventario.InventarioRepositorio;
import com.inventory.repositorios.transferencias.TransferenciaRepositorio;
import com.inventory.modelo.entidades.transferencias.DetalleTransferencia;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteServicioImpl implements ReporteServicio {
    private final VentaRepositorio ventaRepositorio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;
    private final InventarioRepositorio inventarioRepositorio;
    private final TransferenciaRepositorio transferenciaRepositorio;

    /** RF-29/RF-30: Reporte de ventas por período con desglose por sucursal. */
    @Override
    public ReporteVentasDTO generarReporteVentas(Date inicio, Date fin) {
        long total = ventaRepositorio.countByPeriodo(inicio, fin);
        BigDecimal ingreso = ventaRepositorio.sumIngresoPeriodo(inicio, fin);
        BigDecimal promedio = total > 0
                ? ingreso.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Object[]> rawSucursal = ventaRepositorio.findVentasPorSucursalYPeriodo(inicio, fin);
        List<ResumenVentaSucursalDTO> porSucursal = rawSucursal.stream()
                .map(r -> new ResumenVentaSucursalDTO(
                        ((Number) r[0]).longValue(),
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2]))
                .collect(Collectors.toList());

        return new ReporteVentasDTO(inicio, fin, total, ingreso, promedio, porSucursal);
    }

    /** RF-29/RF-30: Reporte de inventario de una sucursal. */
    @Override
    public ReporteInventarioDTO generarReporteInventario(Long idSucursal) {
        var items = inventarioRepositorio.findAll().stream()
                .filter(inv -> idSucursal == null || inv.getSucursalId().equals(idSucursal))
                .collect(Collectors.toList());

        long total = items.size();
        long bajoMinimo = items.stream().filter(i -> i.getStock().compareTo(i.getStockMinimo()) < 0 && i.getStock().compareTo(BigDecimal.ZERO) > 0).count();
        long agotados = items.stream().filter(i -> i.getStock().compareTo(BigDecimal.ZERO) == 0).count();
        BigDecimal valorTotal = items.stream().map(i -> i.getStock()).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ItemInventarioDTO> detalle = items.stream().map(inv -> {
            String estado = inv.getStock().compareTo(BigDecimal.ZERO) == 0 ? "AGOTADO"
                    : inv.getStock().compareTo(inv.getStockMinimo()) < 0 ? "BAJO" : "NORMAL";
            return new ItemInventarioDTO(
                    inv.getProductoId(),
                    "Producto #" + inv.getProductoId(),
                    "SKU-" + inv.getProductoId(),
                    inv.getStock(), inv.getStockMinimo(), estado);
        }).collect(Collectors.toList());

        return new ReporteInventarioDTO(new Date(), idSucursal, total, bajoMinimo, agotados, valorTotal, detalle);
    }

    /** RF-29/RF-30: Reporte de transferencias por período. */
    @Override
    public ReporteTransferenciasDTO generarReporteTransferencias(Date inicio, Date fin) {
        LocalDateTime start = inicio != null ? inicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime end = fin != null ? fin.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
        
        var todas = transferenciaRepositorio.findHistoricalTransfers(null, null, start, end);

        long completadas = todas.stream().filter(t -> "RECIBIDO".equals(t.getEstado())).count();
        long discrepancias = todas.stream().filter(t -> "FALTANTES".equals(t.getEstado())).count();
        long pendientes = todas.stream().filter(t -> "SOLICITADO".equals(t.getEstado()) || "PREPARADO".equals(t.getEstado())).count();

        // Mapeo detallado: una transferencia puede tener múltiples productos
        List<ItemTransferenciaDTO> detalleFormatted = new ArrayList<>();
        
        todas.forEach(t -> {
            for (DetalleTransferencia det : t.getDetalles()) {
                detalleFormatted.add(new ItemTransferenciaDTO(
                    t.getId(), 
                    t.getSucursalOrigenId(), 
                    t.getSucursalDestinoId(), 
                    det.getProductoId(),
                    det.getCantidadSolicitada(), 
                    det.getCantidadRecibida() != null ? det.getCantidadRecibida() : BigDecimal.ZERO, 
                    t.getEstado(),
                    t.getFechaSolicitud(), 
                    t.getEnvio() != null ? t.getEnvio().getFechaRecepcionReal() : null
                ));
            }
        });

        return new ReporteTransferenciasDTO(inicio, fin, todas.size(), completadas, discrepancias, pendientes, detalleFormatted);
    }

    /** RF-31: Comparativo mensual de ventas por año. */
    @Override
    public ReporteComparativoDTO generarComparativoAnual(int anio) {
        List<Object[]> raw = ventaRepositorio.findVentasMensualesPorAnio(anio);
        Map<Integer, BigDecimal> ingresosPorMes = new LinkedHashMap<>();
        Map<Integer, Long> countPorMes = new LinkedHashMap<>();

        for (Object[] row : raw) {
            int mes = ((Number) row[0]).intValue();
            long cnt = ((Number) row[1]).longValue();
            BigDecimal ing = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            ingresosPorMes.put(mes, ing);
            countPorMes.put(mes, cnt);
        }

        List<ResumenMensualDTO> meses = new ArrayList<>();
        BigDecimal ingresoPrevio = null;
        for (int m = 1; m <= 12; m++) {
            BigDecimal ingreso = ingresosPorMes.getOrDefault(m, BigDecimal.ZERO);
            long count = countPorMes.getOrDefault(m, 0L);
            BigDecimal variacion = BigDecimal.ZERO;
            if (ingresoPrevio != null && ingresoPrevio.compareTo(BigDecimal.ZERO) != 0) {
                variacion = ingreso.subtract(ingresoPrevio)
                        .divide(ingresoPrevio, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            String nombre = Month.of(m).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CO"));
            meses.add(new ResumenMensualDTO(m, nombre, count, ingreso, variacion));
            ingresoPrevio = ingreso;
        }

        return new ReporteComparativoDTO(anio, meses);
    }

    /** RF-32: Clasificación ABC de productos por rotación en un mes. */
    @Override
    public ReporteRotacionDTO generarAnalisisRotacion(int mes, int anio) {
        List<Object[]> raw = detalleVentaRepositorio.findRotacionProductosPorMes(mes, anio);
        long totalSalidas = raw.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();

        List<ItemRotacionDTO> items = new ArrayList<>();
        long acumulado = 0;
        for (Object[] row : raw) {
            long salidas = ((Number) row[1]).longValue();
            BigDecimal valor = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            acumulado += salidas;
            double porcentaje = totalSalidas > 0 ? (double) salidas / totalSalidas * 100 : 0;
            double porcentajeAcum = totalSalidas > 0 ? (double) acumulado / totalSalidas * 100 : 0;
            String clasificacion = porcentajeAcum <= 20 ? "A" : porcentajeAcum <= 50 ? "B" : "C";
            items.add(new ItemRotacionDTO(
                    ((Number) row[0]).longValue(),
                    "Producto #" + row[0],
                    salidas, valor,
                    Math.round(porcentaje * 100.0) / 100.0,
                    clasificacion));
        }

        return new ReporteRotacionDTO(anio, mes, items);
    }
}
