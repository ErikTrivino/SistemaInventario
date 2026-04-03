package com.inventory.servicios.implementaciones.ventas;

import com.inventory.modelo.entidades.inventario.Producto;
import com.inventory.modelo.entidades.nucleo.Sucursal;
import com.inventory.modelo.entidades.ventas.DetalleVenta;
import com.inventory.modelo.entidades.ventas.Venta;
import com.inventory.repositorios.inventario.ProductoRepositorio;
import com.inventory.repositorios.nucleo.SucursalRepositorio;
import com.inventory.repositorios.ventas.DetalleVentaRepositorio;
import com.inventory.repositorios.ventas.VentaRepositorio;
import com.inventory.servicios.interfaces.ventas.ComprobanteServicio;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteServicioImpl implements ComprobanteServicio {

    private final VentaRepositorio ventaRepositorio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final SucursalRepositorio sucursalRepositorio;

    private static final String STORAGE_PATH = "storage/comprobantes";

    @Override
    public byte[] generarPdfVenta(Long ventaId) {
        Venta venta = ventaRepositorio.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaId));
        List<DetalleVenta> detalles = detalleVentaRepositorio.findByVentaId(ventaId);
        Sucursal sucursal = sucursalRepositorio.findById(venta.getSucursalId()).orElse(null);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Titulo
            Paragraph title = new Paragraph("SISTEMA DE INVENTARIO - COMPROBANTE DE VENTA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información General
            document.add(new Paragraph("Venta ID: " + venta.getId(), headerFont));
            document.add(new Paragraph("Fecha: " + venta.getFechaVenta(), normalFont));
            if (sucursal != null) {
                document.add(new Paragraph("Sucursal: " + sucursal.getNombre(), normalFont));
                document.add(new Paragraph("Dirección: " + sucursal.getDireccion() + ", " + sucursal.getCiudad(), normalFont));
            }
            document.add(new Paragraph("Vendedor ID: " + venta.getVendedorId(), normalFont));
            document.add(new Paragraph("UUID: " + venta.getComprobanteOriginal(), normalFont));
            document.add(new Paragraph("\n"));

            // Tabla de Productos
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 1.5f, 1.5f, 1.5f});

            String[] headers = {"Producto", "Cant.", "Precio Unit.", "Desc.", "Subtotal"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (DetalleVenta detalle : detalles) {
                Producto p = productoRepositorio.findById(detalle.getProductoId()).orElse(null);
                String nombreP = (p != null) ? p.getNombre() : "Producto ID: " + detalle.getProductoId();
                
                table.addCell(new Phrase(nombreP, normalFont));
                table.addCell(new Phrase(detalle.getCantidad().toString(), normalFont));
                table.addCell(new Phrase("$" + detalle.getPrecioUnitario().toString(), normalFont));
                table.addCell(new Phrase(detalle.getDescuentoAplicado().toString() + "%", normalFont));
                
                java.math.BigDecimal subtotal = detalle.getPrecioUnitario()
                        .multiply(detalle.getCantidad());
                java.math.BigDecimal descAmt = subtotal.multiply(detalle.getDescuentoAplicado())
                        .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                table.addCell(new Phrase("$" + subtotal.subtract(descAmt).toString(), normalFont));
            }

            document.add(table);

            // Total
            Paragraph total = new Paragraph("\nTOTAL: $" + venta.getTotal().toString(), headerFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public String guardarComprobante(byte[] content, String identificador) {
        String fileName = "Comprobante_Venta_" + identificador + ".pdf";
        try {
            Path directory = Paths.get(STORAGE_PATH);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            
            Path filePath = directory.resolve(fileName);
            Files.write(filePath, content);
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error guardando comprobante: " + e.getMessage(), e);
        }
    }

    @Override
    public String obtenerBase64(Long ventaId) {
        Venta venta = ventaRepositorio.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        
        String fileName = venta.getComprobanteOriginal();
        if (fileName == null || fileName.isEmpty()) {
            throw new RuntimeException("El comprobante no ha sido generado para esta venta.");
        }

        try {
            Path filePath = Paths.get(STORAGE_PATH).resolve(fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("El archivo físico del comprobante no existe.");
            }
            byte[] fileContent = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el comprobante: " + e.getMessage(), e);
        }
    }
}
