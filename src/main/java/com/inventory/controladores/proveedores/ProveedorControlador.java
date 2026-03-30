    package com.inventory.controladores.proveedores;
    import com.inventory.servicios.interfaces.proveedores.ProveedorServicio;
    import com.inventory.modelo.dto.proveedores.ProveedorCrearDTO;
    import com.inventory.modelo.dto.proveedores.ProveedorEditarDTO;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping({"/api/proveedores", "/api/proveedores"})
    @RequiredArgsConstructor
    public class ProveedorControlador {
        private final ProveedorServicio supplierService;

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> createSupplier(@RequestBody ProveedorCrearDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, supplierService.createSupplier(dto))); }

        @PutMapping("/{id}")
        public ResponseEntity<MensajeDTO<Object>> updateSupplier(@PathVariable Long id, @RequestBody ProveedorEditarDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, supplierService.updateSupplier(id, dto))); }

        @GetMapping
        public ResponseEntity<MensajeDTO<Object>> getSuppliers() { return ResponseEntity.ok(new MensajeDTO<>(false, supplierService.getSuppliers())); }

        @PostMapping({"/{id}/asignar-producto", "/{id}/assign-product"})
        public ResponseEntity<MensajeDTO<Object>> assignProductToSupplier(@PathVariable Long id, @RequestParam Long productId) {
            supplierService.assignProductToSupplier(id, productId);
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
        }
    }





