    package com.inventory.controladores.compras;
    import com.inventory.servicios.interfaces.compras.CompraServicio;
    import com.inventory.modelo.dto.compras.CompraCrearDTO;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping({"/api/compras", "/api/compras"})
    @RequiredArgsConstructor
    public class CompraControlador {
        private final CompraServicio purchaseService;

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> createPurchase(@RequestBody CompraCrearDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, purchaseService.createPurchase(dto))); }

        @PutMapping({"/{id}/recibir", "/{id}/receive"})
        public ResponseEntity<MensajeDTO<Object>> receivePurchase(@PathVariable Long id) { purchaseService.receivePurchase(id); return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); }

        @GetMapping({"/proveedor/{id}", "/supplier/{id}"})
        public ResponseEntity<MensajeDTO<Object>> getPurchasesBySupplier(@PathVariable Long id) { return ResponseEntity.ok(new MensajeDTO<>(false, purchaseService.getPurchasesBySupplier(id))); }
    }





