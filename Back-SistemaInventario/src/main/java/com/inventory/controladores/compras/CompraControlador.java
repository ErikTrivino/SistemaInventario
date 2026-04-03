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
        private final CompraServicio compraServicio;

        @PostMapping
        public ResponseEntity<MensajeDTO<Object>> crearCompra(@Valid @RequestBody OrdenCompraCrearDTO dto) { 
            // Mocking userId extraction, assuming user ID 1 for now like in previous methods
            Long userId = 1L; 
            return ResponseEntity.ok(new MensajeDTO<>(false, compraServicio.crearCompra(dto, userId))); 
        }

        @PostMapping({"/recepcion", "/receive"})
        public ResponseEntity<MensajeDTO<Object>> recibirCompra(@Valid @RequestBody OrdenCompraRecepcionDTO dto) { 
            compraServicio.recibirCompra(dto); 
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); 
        }

        @GetMapping({"/historico", "/history"})
        public ResponseEntity<MensajeDTO<Object>> obtenerHistoricoCompras(
            @RequestParam(required = false) Long idProveedor,
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaHasta,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina
        ) { 
            return ResponseEntity.ok(new MensajeDTO<>(false, compraServicio.obtenerHistoricoCompras(idProveedor, idProducto, estado, idSucursal, fechaDesde, fechaHasta, pagina, porPagina)));
        }
    }





