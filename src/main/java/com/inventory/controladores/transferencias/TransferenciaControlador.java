package com.inventory.controladores.transferencias;

import com.inventory.servicios.interfaces.transferencias.TransferenciaServicio;
import com.inventory.modelo.dto.transferencias.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transferencias")
@RequiredArgsConstructor
public class TransferenciaControlador {
    private final TransferenciaServicio transferService;

    /** RF-18: Solicitar transferencia entre sucursales. */
    @PostMapping("/solicitar")
    public ResponseEntity<MensajeDTO<Object>> solicitar(@Valid @RequestBody TransferenciaCrearDTO dto) {
        Long userId = 1L; // Mock userId, reemplazar con extracción JWT
        return ResponseEntity.ok(new MensajeDTO<>(false, transferService.requestTransfer(dto, userId)));
    }

    /** RF-21: Preparar envío con cantidad confirmada. */
    @PutMapping("/preparar")
    public ResponseEntity<MensajeDTO<Object>> preparar(@Valid @RequestBody TransferenciaPrepararDTO dto) {
        return ResponseEntity.ok(new MensajeDTO<>(false, transferService.prepareTransfer(dto)));
    }

    /** RF-19: Confirmar envío — descuenta stock en origen. */
    @PutMapping("/enviar")
    public ResponseEntity<MensajeDTO<Object>> enviar(@Valid @RequestBody TransferenciaConfirmarEnvioDTO dto) {
        return ResponseEntity.ok(new MensajeDTO<>(false, transferService.shipTransfer(dto)));
    }

    /** RF-20: Confirmar recepción — suma stock en destino y registra discrepancias. */
    @PutMapping("/recibir")
    public ResponseEntity<MensajeDTO<Object>> recibir(@Valid @RequestBody TransferenciaRecepcionDTO dto) {
        return ResponseEntity.ok(new MensajeDTO<>(false, transferService.receiveTransfer(dto)));
    }

    /** RF-23: Histórico filtrado de transferencias de la sucursal. */
    @GetMapping("/historico")
    public ResponseEntity<MensajeDTO<Object>> historico(
            @RequestParam Long branchId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false) Integer pagina
    ) {
        return ResponseEntity.ok(new MensajeDTO<>(false, transferService.getTransfers(branchId, estado, desde, hasta, pagina, porPagina)));
    }
}





