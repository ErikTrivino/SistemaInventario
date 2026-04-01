package com.inventory.controladores.reportes;

import com.inventory.servicios.interfaces.reportes.ReporteServicio;
import com.inventory.modelo.dto.comun.MensajeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteControlador {
    private final ReporteServicio reporteServicio;

    /** RF-29/RF-30: Reporte de ventas por período con desglose por sucursal. */
    @GetMapping("/ventas")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    public ResponseEntity<MensajeDTO<Object>> reporteVentas(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.generarReporteVentas(inicio, fin, pagina, porPagina)));
    }

    /** RF-29/RF-30: Reporte de inventario por sucursal. */
    @GetMapping("/inventario")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    public ResponseEntity<MensajeDTO<Object>> reporteInventario(
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.generarReporteInventario(idSucursal, pagina, porPagina)));
    }

    /** RF-29/RF-30: Reporte de transferencias entre sucursales por período. */
    @GetMapping("/transferencias")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    public ResponseEntity<MensajeDTO<Object>> reporteTransferencias(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fin,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.generarReporteTransferencias(inicio, fin, pagina, porPagina)));
    }

    /** RF-31: Comparativo de ventas mensual por año. */
    @GetMapping("/comparativo/{anio}")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    public ResponseEntity<MensajeDTO<Object>> comparativoAnual(@PathVariable int anio) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.generarComparativoAnual(anio)));
    }

    /** RF-32: Análisis de rotación ABC de productos en un mes/año. */
    @GetMapping("/rotacion")
    @PreAuthorize("hasAnyRole('GERENTE', 'ADMIN')")
    public ResponseEntity<MensajeDTO<Object>> analisisRotacion(
            @RequestParam int mes,
            @RequestParam int anio,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity.ok(new MensajeDTO<>(false, reporteServicio.generarAnalisisRotacion(mes, anio, pagina, porPagina)));
    }
}





