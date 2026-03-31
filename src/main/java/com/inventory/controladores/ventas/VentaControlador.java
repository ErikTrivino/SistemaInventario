    package com.inventory.controladores.ventas;
    import com.inventory.servicios.interfaces.ventas.VentaServicio;
    import com.inventory.modelo.dto.ventas.VentaCrearDTO;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.web.bind.annotation.*;
    import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import jakarta.validation.Valid;
    import java.math.BigDecimal;
    import java.util.Date;

    @RestController
    @RequestMapping({"/api/ventas", "/api/sales"})
    @RequiredArgsConstructor
    public class VentaControlador {
        private final VentaServicio saleService;

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> createSale(@Valid @RequestBody VentaCrearDTO dto) { 
            Long userId = 1L; // Mock userId
            return ResponseEntity.ok(new MensajeDTO<>(false, saleService.createSale(dto, userId))); 
        }

        @GetMapping("/validacion-stock")
        public ResponseEntity<MensajeDTO<Object>> checkStock(
            @RequestParam Long productId,
            @RequestParam Long branchId,
            @RequestParam BigDecimal quantity
        ) {
            return ResponseEntity.ok(new MensajeDTO<>(false, saleService.validateStock(productId, branchId, quantity)));
        }

        @GetMapping({"/sucursal/{id}", "/branch/{id}"})
        public ResponseEntity<MensajeDTO<Object>> getSalesByBranch(
                @PathVariable Long id,
                @RequestParam(required = false, defaultValue = "10") Integer porPagina,
                @RequestParam(required = false) Integer pagina) { 
            return ResponseEntity.ok(new MensajeDTO<>(false, saleService.getSalesByBranch(id, pagina, porPagina))); 
        }

        @GetMapping({"/rango-fechas", "/date-range"})
        public ResponseEntity<MensajeDTO<Object>> getSalesByDateRange(
                @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date start, 
                @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date end,
                @RequestParam(required = false, defaultValue = "10") Integer porPagina,
                @RequestParam(required = false) Integer pagina) {
            return ResponseEntity.ok(new MensajeDTO<>(false, saleService.getSalesByDateRange(start, end, pagina, porPagina)));
        }
    }





