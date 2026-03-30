    package com.inventory.controladores.inventario;
    import com.inventory.servicios.interfaces.inventario.InventarioServicio;
    import com.inventory.modelo.dto.inventario.ProductoCrearDTO;
    import com.inventory.modelo.dto.inventario.ProductoEditarDTO;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping("/api")
    @RequiredArgsConstructor
    public class InventarioControlador {
        private final InventarioServicio inventoryService;

        @PostMapping({"/productos", "/products"})
        public ResponseEntity<MensajeDTO<Object>> createProduct(@RequestBody ProductoCrearDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.createProduct(dto))); }

        @GetMapping({"/productos", "/products"})
        public ResponseEntity<MensajeDTO<Object>> getProducts() { return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.getProducts())); }

        @PutMapping({"/productos/{id}", "/products/{id}"})
        public ResponseEntity<MensajeDTO<Object>> updateProduct(@PathVariable Long id, @RequestBody ProductoEditarDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.updateProduct(id, dto))); }

        @DeleteMapping({"/productos/{id}", "/products/{id}"})
        public ResponseEntity<MensajeDTO<Object>> deleteProduct(@PathVariable Long id) { inventoryService.deleteProduct(id); return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); }

        @GetMapping({"/inventario/{branchId}", "/inventory/{branchId}"})
        public ResponseEntity<MensajeDTO<Object>> getInventoryByBranch(@PathVariable Long branchId) { return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.getInventoryByBranch(branchId))); }

        @PutMapping({"/inventario/actualizar-stock", "/inventory/update-stock"})
        public ResponseEntity<MensajeDTO<Object>> updateStock(@RequestParam Long productId, @RequestParam Long branchId, @RequestParam Double quantity, @RequestParam String type, @RequestParam String reason) {
            inventoryService.updateStock(productId, branchId, quantity, type, reason);
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
        }
    }





