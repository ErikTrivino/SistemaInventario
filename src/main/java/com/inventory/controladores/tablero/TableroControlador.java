    package com.inventory.controladores.tablero;
    import com.inventory.servicios.interfaces.tablero.TableroServicio;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping({"/api/tablero", "/api/tablero"})
    @RequiredArgsConstructor
    public class TableroControlador {
        private final TableroServicio dashboardService;

        @GetMapping({"/ventas", "/sales"})
        public ResponseEntity<MensajeDTO<Object>> getSalesSummary() { return ResponseEntity.ok(new MensajeDTO<>(false, dashboardService.getSalesSummary())); }

        @GetMapping({"/inventario", "/inventory"})
        public ResponseEntity<MensajeDTO<Object>> getInventoryMetrics() { return ResponseEntity.ok(new MensajeDTO<>(false, dashboardService.getInventoryMetrics())); }

        @GetMapping({"/transferencias", "/transfers"})
        public ResponseEntity<MensajeDTO<Object>> getTransferStatusSummary() { return ResponseEntity.ok(new MensajeDTO<>(false, dashboardService.getTransferStatusSummary())); }
    }





