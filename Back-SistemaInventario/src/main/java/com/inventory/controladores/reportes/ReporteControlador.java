package com.inventory.controladores.reportes;

import com.inventory.servicios.interfaces.reportes.ReporteServicio;
import com.inventory.modelo.dto.comun.MensajeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteControlador {
    private final ReporteServicio reporteServicio;

    // --- REPORTES DE INVENTARIO ---

    /** RF-29/RF-30: Reporte de inventario por sucursal. */
    @GetMapping("/inventario")
    public ResponseEntity<MensajeDTO<Object>> reporteInventario(
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity
                .ok(new MensajeDTO<>(false, reporteServicio.generarReporteInventario(idSucursal, pagina, porPagina)));
    }

    /** Exportación a PDF (Base64) del reporte de inventario. */
    @GetMapping("/inventario/pdf")
    public ResponseEntity<MensajeDTO<String>> reporteInventarioPdf(
            @RequestParam(required = false) Long idSucursal) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.obtenerBase64ReporteInventario(idSucursal)));
    }

    // --- REPORTES DE VENTAS ---

    /** RF-29/RF-30: Reporte de ventas por período con desglose por sucursal. */
    @GetMapping("/ventas")
    public ResponseEntity<MensajeDTO<Object>> reporteVentas(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity
                .ok(new MensajeDTO<>(false, reporteServicio.generarReporteVentas(inicio, fin, pagina, porPagina)));
    }

    /** Exportación a PDF (Base64) del reporte de ventas. */
    @GetMapping("/ventas/pdf")
    public ResponseEntity<MensajeDTO<String>> reporteVentasPdf(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.obtenerBase64ReporteVentas(inicio, fin)));
    }

    // --- REPORTES DE TRANSFERENCIAS ---

    /** RF-29/RF-30: Reporte de transferencias entre sucursales por período. */
    @GetMapping("/transferencias")
    public ResponseEntity<MensajeDTO<Object>> reporteTransferencias(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity.ok(
                new MensajeDTO<>(false, reporteServicio.generarReporteTransferencias(inicio, fin, pagina, porPagina)));
    }

    /** Exportación a PDF (Base64) del reporte de transferencias. */
    @GetMapping("/transferencias/pdf")
    public ResponseEntity<MensajeDTO<String>> reporteTransferenciasPdf(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.obtenerBase64ReporteTransferencias(inicio, fin)));
    }

    // --- COMPARATIVO ANUAL ---

    /** RF-31: Comparativo de ventas mensual por año. */
    @GetMapping("/comparativo/{anio}")
    public ResponseEntity<MensajeDTO<Object>> comparativoAnual(@PathVariable int anio) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.generarComparativoAnual(anio)));
    }

    /** Exportación a PDF (Base64) del reporte comparativo anual. */
    @GetMapping("/comparativo/{anio}/pdf")
    public ResponseEntity<MensajeDTO<String>> comparativoAnualPdf(@PathVariable int anio) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.obtenerBase64ComparativoAnual(anio)));
    }

    // --- ANÁLISIS DE ROTACIÓN ---

    /** RF-32: Análisis de rotación ABC de productos en un mes/año. */
    @GetMapping("/rotacion")
    public ResponseEntity<MensajeDTO<Object>> analisisRotacion(
            @RequestParam int mes,
            @RequestParam int anio,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity
                .ok(new MensajeDTO<>(false, reporteServicio.generarAnalisisRotacion(mes, anio, pagina, porPagina)));
    }

    /** Exportación a PDF (Base64) del análisis de rotación ABC. */
    @GetMapping("/rotacion/pdf")
    public ResponseEntity<MensajeDTO<String>> analisisRotacionPdf(
            @RequestParam int mes,
            @RequestParam int anio) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.obtenerBase64AnalisisRotacion(mes, anio)));
    }
}
