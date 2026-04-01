    package com.inventory.controladores.logistica;
    import com.inventory.servicios.interfaces.logistica.LogisticaServicio;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping({"/api/envios", "/api/shipments"})
    @RequiredArgsConstructor
    public class LogisticaControlador {
        private final LogisticaServicio logisticsService;

        @GetMapping
        public ResponseEntity<MensajeDTO<Object>> getShipments(
                @RequestParam(required = false, defaultValue = "10") Integer porPagina,
                @RequestParam(required = false) Integer pagina) {
            return ResponseEntity.ok(new MensajeDTO<>(false, logisticsService.getShipments(pagina, porPagina)));
        }

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> createShipment(@RequestParam Long transferId) { return ResponseEntity.ok(new MensajeDTO<>(false, logisticsService.createShipment(transferId))); }

        @PutMapping({"/{id}/estado", "/{id}/status"})
        public ResponseEntity<MensajeDTO<Object>> updateShipmentStatus(@PathVariable Long id, @RequestParam String status) {
            logisticsService.updateShipmentStatus(id, status);
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
        }

        @GetMapping({"/metricas", "/metrics"})
        public ResponseEntity<MensajeDTO<Object>> getMetrics() {
            logisticsService.compareDeliveryTimes();
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
        }
    }





