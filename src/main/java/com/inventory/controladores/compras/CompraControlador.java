    package com.inventory.controladores.compras;
    import com.inventory.servicios.interfaces.compras.CompraServicio;
    import com.inventory.modelo.dto.compras.OrdenCompraCrearDTO;
    import com.inventory.modelo.dto.compras.OrdenCompraRecepcionDTO;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.format.annotation.DateTimeFormat;
    import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import jakarta.validation.Valid;

    @RestController
    @RequestMapping({"/api/compras", "/api/purchases"})
    @RequiredArgsConstructor
    public class CompraControlador {
        private final CompraServicio purchaseService;

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> createPurchase(@Valid @RequestBody OrdenCompraCrearDTO dto) { 
            // Mocking userId extraction, assuming user ID 1 for now like in previous methods
            Long userId = 1L; 
            return ResponseEntity.ok(new MensajeDTO<>(false, purchaseService.createPurchase(dto, userId))); 
        }

        @PostMapping({"/recepcion", "/receive"})
        public ResponseEntity<MensajeDTO<Object>> receivePurchase(@Valid @RequestBody OrdenCompraRecepcionDTO dto) { 
            purchaseService.receivePurchase(dto); 
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); 
        }

        @GetMapping({"/historico", "/history"})
        public ResponseEntity<MensajeDTO<Object>> getHistoricalPurchases(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina
        ) { 
            return ResponseEntity.ok(new MensajeDTO<>(false, purchaseService.getPurchaseHistory(supplierId, productId, startDate, endDate, pagina, porPagina))); 
        }
    }





