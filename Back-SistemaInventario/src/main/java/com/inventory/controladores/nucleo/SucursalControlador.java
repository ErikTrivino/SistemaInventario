package com.inventory.controladores.nucleo;

import com.inventory.modelo.dto.comun.MensajeDTO;
import com.inventory.modelo.dto.nucleo.SucursalResponseDTO;
import com.inventory.servicios.interfaces.nucleo.SucursalServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalControlador {

    private final SucursalServicio sucursalServicio;

    @GetMapping
    public ResponseEntity<MensajeDTO<List<SucursalResponseDTO>>> listarTodas() {
        return ResponseEntity.ok(new MensajeDTO<>(false, sucursalServicio.listarTodas()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<SucursalResponseDTO>> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new MensajeDTO<>(false, sucursalServicio.consultarPorId(id)));
    }
}
