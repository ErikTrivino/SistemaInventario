    package com.inventory.controladores.reportes;
    import com.inventory.servicios.interfaces.reportes.ReporteServicio;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import java.util.Date;

    @RestController
    @RequestMapping({"/api/reportes", "/api/reportes"})
    @RequiredArgsConstructor
    public class ReporteControlador {
        private final ReporteServicio reportService;

        @GetMapping({"/inventario", "/inventory"})
        public ResponseEntity<MensajeDTO<Object>> getInventoryReport(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date end) {
            return ResponseEntity.ok(new MensajeDTO<>(false, reportService.generateInventoryReport(start, end)));
        }

        @GetMapping({"/ventas", "/sales"})
        public ResponseEntity<MensajeDTO<Object>> getSalesReport(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date end) {
            return ResponseEntity.ok(new MensajeDTO<>(false, reportService.generateSalesReport(start, end)));
        }

        @GetMapping({"/transferencias", "/transfers"})
        public ResponseEntity<MensajeDTO<Object>> getTransferReport(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date end) {
            return ResponseEntity.ok(new MensajeDTO<>(false, reportService.generateTransferReport(start, end)));
        }
    }





