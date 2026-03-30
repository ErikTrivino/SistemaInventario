    package com.inventory.controladores.ventas;
    import com.inventory.servicios.interfaces.ventas.VentaServicio;
    import com.inventory.modelo.dto.ventas.VentaCrearDTO;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import java.util.Date;

    @RestController
    @RequestMapping({"/api/ventas", "/api/ventas"})
    @RequiredArgsConstructor
    public class VentaControlador {
        private final VentaServicio saleService;

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> createSale(@RequestBody VentaCrearDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, saleService.createSale(dto))); }

        @GetMapping({"/sucursal/{id}", "/branch/{id}"})
        public ResponseEntity<MensajeDTO<Object>> getSalesByBranch(@PathVariable Long id) { return ResponseEntity.ok(new MensajeDTO<>(false, saleService.getSalesByBranch(id))); }

        @GetMapping({"/rango-fechas", "/date-range"})
        public ResponseEntity<MensajeDTO<Object>> getSalesByDateRange(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date start, @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date end) {
            return ResponseEntity.ok(new MensajeDTO<>(false, saleService.getSalesByDateRange(start, end)));
        }
    }





