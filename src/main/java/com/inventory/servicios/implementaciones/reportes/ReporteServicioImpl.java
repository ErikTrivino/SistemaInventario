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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ReporteVentasDTO generarReporteVentas(Date inicio, Date fin, Integer pagina, Integer porPagina) {
        long total = ventaRepositorio.countByPeriodo(inicio, fin);
        BigDecimal ingreso = ventaRepositorio.sumIngresoPeriodo(inicio, fin);
        BigDecimal promedio = total > 0
                ? ingreso.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Object[]> rawSucursal = ventaRepositorio.findVentasPorSucursalYPeriodo(inicio, fin);
        List<ResumenVentaSucursalDTO> porSucursalList = rawSucursal.stream()
                .map(r -> new ResumenVentaSucursalDTO(
                        ((Number) r[0]).longValue(),
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2]))
                .collect(Collectors.toList());

        Page<ResumenVentaSucursalDTO> porSucursalPage = paginateList(porSucursalList, pagina, porPagina);

        return new ReporteVentasDTO(inicio, fin, total, ingreso, promedio, porSucursalPage);
    }

    /** RF-29/RF-30: Reporte de inventario de una sucursal. */
    @Override
    public ReporteInventarioDTO generarReporteInventario(Long idSucursal, Integer pagina, Integer porPagina) {
        var itemsAll = inventarioRepositorio.findAll().stream()
                .filter(inv -> idSucursal == null || inv.getSucursalId().equals(idSucursal))
                .collect(Collectors.toList());

        long total = itemsAll.size();
        long bajoMinimo = itemsAll.stream().filter(i -> i.getStock().compareTo(i.getStockMinimo()) < 0 && i.getStock().compareTo(BigDecimal.ZERO) > 0).count();
        long agotados = itemsAll.stream().filter(i -> i.getStock().compareTo(BigDecimal.ZERO) == 0).count();
        BigDecimal valorTotal = itemsAll.stream().map(i -> i.getStock()).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ItemInventarioDTO> detalleList = itemsAll.stream().map(inv -> {
            String estado = inv.getStock().compareTo(BigDecimal.ZERO) == 0 ? "AGOTADO"
                    : inv.getStock().compareTo(inv.getStockMinimo()) < 0 ? "BAJO" : "NORMAL";
            return new ItemInventarioDTO(
                    inv.getProductoId(),
                    "Producto #" + inv.getProductoId(),
                    "SKU-" + inv.getProductoId(),
                    inv.getStock(), inv.getStockMinimo(), estado);
        }).collect(Collectors.toList());

        Page<ItemInventarioDTO> detallePage = paginateList(detalleList, pagina, porPagina);

        return new ReporteInventarioDTO(new Date(), idSucursal, total, bajoMinimo, agotados, valorTotal, detallePage);
    }

    /** RF-29/RF-30: Reporte de transferencias por período. */
    @Override
    public ReporteTransferenciasDTO generarReporteTransferencias(Date inicio, Date fin, Integer pagina, Integer porPagina) {
        LocalDateTime start = inicio != null ? inicio.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime end = fin != null ? fin.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
        
        Pageable unpaged = Pageable.unpaged();
        var todas = transferenciaRepositorio.findHistoricalTransfers(null, null, start, end, unpaged).getContent();

        long completadas = todas.stream().filter(t -> "RECIBIDO".equals(t.getEstado())).count();
        long discrepancias = todas.stream().filter(t -> "FALTANTES".equals(t.getEstado())).count();
        long pendientes = todas.stream().filter(t -> "SOLICITADO".equals(t.getEstado()) || "PREPARADO".equals(t.getEstado())).count();

        // Mapeo detallado: una transferencia puede tener múltiples productos
        List<ItemTransferenciaDTO> detalleFull = new ArrayList<>();
        
        todas.forEach(t -> {
            for (DetalleTransferencia det : t.getDetalles()) {
                detalleFull.add(new ItemTransferenciaDTO(
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

        Page<ItemTransferenciaDTO> detallePage = paginateList(detalleFull, pagina, porPagina);

        return new ReporteTransferenciasDTO(inicio, fin, (int) todas.size(), completadas, discrepancias, pendientes, detallePage);
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
    public ReporteRotacionDTO generarAnalisisRotacion(int mes, int anio, Integer pagina, Integer porPagina) {
        List<Object[]> raw = detalleVentaRepositorio.findRotacionProductosPorMes(mes, anio);
        long totalSalidas = raw.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();

        List<ItemRotacionDTO> itemsFull = new ArrayList<>();
        long acumulado = 0;
        for (Object[] row : raw) {
            long salidas = ((Number) row[1]).longValue();
            BigDecimal valor = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            acumulado += salidas;
            double porcentaje = totalSalidas > 0 ? (double) salidas / totalSalidas * 100 : 0;
            double porcentajeAcum = totalSalidas > 0 ? (double) acumulado / totalSalidas * 100 : 0;
            String clasificacion = porcentajeAcum <= 20 ? "A" : porcentajeAcum <= 50 ? "B" : "C";
            itemsFull.add(new ItemRotacionDTO(
                    ((Number) row[0]).longValue(),
                    "Producto #" + row[0],
                    salidas, valor,
                    Math.round(porcentaje * 100.0) / 100.0,
                    clasificacion));
        }

        Page<ItemRotacionDTO> itemsPage = paginateList(itemsFull, pagina, porPagina);

        return new ReporteRotacionDTO(anio, mes, itemsPage);
    }

    private <T> Page<T> paginateList(List<T> list, Integer pagina, Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        List<T> sublist = (start <= end && start <= list.size()) ? list.subList(start, end) : new ArrayList<>();
        return new PageImpl<>(sublist, pageable, list.size());
    }
}
