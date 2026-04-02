package com.inventory.controladores.inventario;

import com.inventory.servicios.interfaces.inventario.MovimientoServicio;
import com.inventory.modelo.dto.inventario.MovimientoRetiroDTO;
import com.inventory.modelo.dto.comun.MensajeDTO;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoControlador {
    private final MovimientoServicio movimientoServicio;

    @PostMapping("/retiro")
    public ResponseEntity<MensajeDTO<Object>> registrarRetiro(@Valid @RequestBody MovimientoRetiroDTO dto) {
        movimientoServicio.registrarRetiro(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Retiro registrado exitosamente"));
    }
}
