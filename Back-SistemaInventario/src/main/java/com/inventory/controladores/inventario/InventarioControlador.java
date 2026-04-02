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

    @PostMapping({ "/productos" })
    public ResponseEntity<MensajeDTO<Object>> createProduct(@RequestBody ProductoCrearDTO dto) {
        return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.createProduct(dto)));
    }

    @GetMapping({ "/productos" })
    public ResponseEntity<MensajeDTO<Object>> getProducts(
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.getProducts(pagina, porPagina)));
    }

    @GetMapping({ "/productos/{idSucursal}/{idProducto}" })
    public ResponseEntity<MensajeDTO<Object>> getProductById(@PathVariable Long idSucursal,@PathVariable Long idProducto) {
        return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.getProductByIdSucursal(idSucursal, idProducto)));
    }

    @PutMapping({ "/productos/{id}" })
    public ResponseEntity<MensajeDTO<Object>> updateProduct(@PathVariable Long id, @RequestBody ProductoEditarDTO dto) {
        return ResponseEntity.ok(new MensajeDTO<>(false, inventoryService.updateProduct(id, dto)));
    }

    @DeleteMapping({ "/productos/{id}" })
    public ResponseEntity<MensajeDTO<Object>> deleteProduct(@PathVariable Long id) {
        inventoryService.deleteProduct(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
    }

    @GetMapping({ "/inventario/{branchId}" })

    public ResponseEntity<MensajeDTO<Object>> getInventoryByBranch(
            @PathVariable Long branchId,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity
                .ok(new MensajeDTO<>(false, inventoryService.getInventoryByBranch(branchId, pagina, porPagina)));
    }

    @GetMapping({ "/catalogo/{branchId}" })

    public ResponseEntity<MensajeDTO<Object>> getCatalogo(
            @PathVariable Long branchId,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina) {
        return ResponseEntity
                .ok(new MensajeDTO<>(false, inventoryService.getCatalogoActivo(branchId, pagina, porPagina)));
    }

    @PutMapping({ "/inventario/actualizar-stock" })

    public ResponseEntity<MensajeDTO<Object>> updateStock(@RequestParam Long productId, @RequestParam Long branchId,
            @RequestParam Double quantity, @RequestParam String type, @RequestParam String reason,
            @RequestParam(required = false, defaultValue = "sistema") String usuarioResponsable) {
        inventoryService.updateStock(productId, branchId, quantity, type, reason, usuarioResponsable);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
    }
}
