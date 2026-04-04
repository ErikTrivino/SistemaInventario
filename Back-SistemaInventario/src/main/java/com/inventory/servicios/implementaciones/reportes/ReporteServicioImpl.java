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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;

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

    private <T> Page<T> paginateList(java.util.List<T> list, Integer pagina, Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        
        java.util.List<T> sublist = (start <= end && start <= list.size()) ? list.subList(start, end) : new ArrayList<>();
        return new PageImpl<>(sublist, pageable, list.size());
    }

    // --- Implementación de Exportación a PDF Base64 ---

    @Override
    public String obtenerBase64ReporteVentas(Date inicio, Date fin) {
        ReporteVentasDTO dto = generarReporteVentas(inicio, fin, 1, Integer.MAX_VALUE);
        return generarPdfBase64("REPORTE DE VENTAS POR PERIODO", document -> {
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            document.add(new Paragraph("Periodo: " + inicio + " al " + fin, normalFont));
            document.add(new Paragraph("Total Ventas: " + dto.totalVentas(), normalFont));
            document.add(new Paragraph("Ingreso Total: $" + dto.ingresoTotal(), normalFont));
            document.add(new Paragraph("Promedio Venta: $" + dto.promedioVenta(), normalFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "ID Sucursal", "Cantidad Ventas", "Total Ingresos");

            for (ResumenVentaSucursalDTO s : dto.porSucursal().getContent()) {
                table.addCell(new Phrase(s.idSucursal().toString(), normalFont));
                table.addCell(new Phrase(String.valueOf(s.cantidadVentas()), normalFont));
                table.addCell(new Phrase("$" + s.ingresoTotal().toString(), normalFont));
            }
            document.add(table);
        });
    }

    @Override
    public String obtenerBase64ReporteInventario(Long idSucursal) {
        ReporteInventarioDTO dto = generarReporteInventario(idSucursal, 1, Integer.MAX_VALUE);
        String titulo = idSucursal == null ? "REPORTE DE INVENTARIO GLOBAL" : "REPORTE DE INVENTARIO - SUCURSAL " + idSucursal;
        
        return generarPdfBase64(titulo, document -> {
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            document.add(new Paragraph("Fecha Generación: " + dto.fechaGeneracion(), normalFont));
            document.add(new Paragraph("Total Productos: " + dto.totalProductos(), normalFont));
            document.add(new Paragraph("En stock mínimo: " + dto.productosEnStockMinimo(), normalFont));
            document.add(new Paragraph("Productos agotados: " + dto.productosAgotados(), normalFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "Producto", "SKU", "Stock", "Mínimo", "Estado");

            for (ItemInventarioDTO item : dto.detalle().getContent()) {
                table.addCell(new Phrase(item.nombreProducto(), normalFont));
                table.addCell(new Phrase(item.sku(), normalFont));
                table.addCell(new Phrase(item.stockActual().toString(), normalFont));
                table.addCell(new Phrase(item.stockMinimo().toString(), normalFont));
                table.addCell(new Phrase(item.estadoStock(), normalFont));
            }
            document.add(table);
        });
    }

    @Override
    public String obtenerBase64ReporteTransferencias(Date inicio, Date fin) {
        ReporteTransferenciasDTO dto = generarReporteTransferencias(inicio, fin, 1, Integer.MAX_VALUE);
        return generarPdfBase64("REPORTE DE TRANSFERENCIAS POR PERIODO", document -> {
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            document.add(new Paragraph("Periodo: " + inicio + " al " + fin, normalFont));
            document.add(new Paragraph("Total Transferencias: " + dto.totalTransferencias(), normalFont));
            document.add(new Paragraph("Completadas: " + dto.completadas(), normalFont));
            document.add(new Paragraph("Pendientes: " + dto.pendientes(), normalFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "ID", "Origen", "Destino", "Cant. Sol.", "Estado");

            for (ItemTransferenciaDTO t : dto.detalle().getContent()) {
                table.addCell(new Phrase(t.idTransferencia().toString(), normalFont));
                table.addCell(new Phrase(t.idSucursalOrigen().toString(), normalFont));
                table.addCell(new Phrase(t.idSucursalDestino().toString(), normalFont));
                table.addCell(new Phrase(t.cantidadSolicitada().toString(), normalFont));
                table.addCell(new Phrase(t.estado(), normalFont));
            }
            document.add(table);
        });
    }

    @Override
    public String obtenerBase64ComparativoAnual(int anio) {
        ReporteComparativoDTO dto = generarComparativoAnual(anio);
        return generarPdfBase64("REPORTE COMPARATIVO DE VENTAS - AÑO " + anio, document -> {
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "Mes", "Ventas", "Ingresos", "Variación");

            for (ResumenMensualDTO m : dto.meses()) {
                table.addCell(new Phrase(m.nombreMes(), normalFont));
                table.addCell(new Phrase(String.valueOf(m.cantidadVentas()), normalFont));
                table.addCell(new Phrase("$" + m.ingresoTotal().toString(), normalFont));
                table.addCell(new Phrase(m.variacionPorcentual().toString() + "%", normalFont));
            }
            document.add(table);
        });
    }

    @Override
    public String obtenerBase64AnalisisRotacion(int mes, int anio) {
        ReporteRotacionDTO dto = generarAnalisisRotacion(mes, anio, 1, Integer.MAX_VALUE);
        return generarPdfBase64("ANALISIS DE ROTACION ABC - MES " + mes + "/" + anio, document -> {
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "Producto", "Salidas", "Valor Total", "ABC");

            for (ItemRotacionDTO item : dto.productos().getContent()) {
                table.addCell(new Phrase(item.nombreProducto(), normalFont));
                table.addCell(new Phrase(String.valueOf(item.totalSalidas()), normalFont));
                table.addCell(new Phrase("$" + item.valorTotalSalidas().toString(), normalFont));
                table.addCell(new Phrase(item.clasificacion(), normalFont));
            }
            document.add(table);
        });
    }

    private String generarPdfBase64(String subTitulo, PdfContentGenerator generator) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("SISTEMA DE INVENTARIO - " + subTitulo, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            generator.generate(document);

            document.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF Base64: " + e.getMessage(), e);
        }
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    @FunctionalInterface
    private interface PdfContentGenerator {
        void generate(Document document) throws DocumentException;
    }
}
