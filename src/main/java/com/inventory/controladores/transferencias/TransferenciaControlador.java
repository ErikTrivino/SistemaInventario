    package com.inventory.controladores.transferencias;
    import com.inventory.servicios.interfaces.transferencias.TransferenciaServicio;
    import com.inventory.modelo.dto.transferencias.TransferenciaCrearDTO;
    import com.inventory.modelo.dto.transferencias.TransferenciaRecepcionParcialDTO;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping({"/api/transferencias", "/api/transferencias"})
    @RequiredArgsConstructor
    public class TransferenciaControlador {
        private final TransferenciaServicio transferService;

        @PostMapping({"/solicitar", "/request"})
        public ResponseEntity<MensajeDTO<Object>> requestTransfer(@RequestBody TransferenciaCrearDTO dto) { return ResponseEntity.ok(new MensajeDTO<>(false, transferService.requestTransfer(dto))); }

        @PutMapping({"/{id}/aprobar", "/{id}/approve"})
        public ResponseEntity<MensajeDTO<Object>> approveTransfer(@PathVariable Long id) { transferService.approveTransfer(id); return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); }

        @PutMapping({"/{id}/enviar", "/{id}/ship"})
        public ResponseEntity<MensajeDTO<Object>> shipTransfer(@PathVariable Long id) { transferService.shipTransfer(id); return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); }

        @PutMapping({"/{id}/recibir", "/{id}/receive"})
        public ResponseEntity<MensajeDTO<Object>> receiveTransfer(@PathVariable Long id) { transferService.receiveTransfer(id); return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa")); }

        @PutMapping({"/{id}/recibir-parcial", "/{id}/receive-partial"})
        public ResponseEntity<MensajeDTO<Object>> receivePartialTransfer(@PathVariable Long id, @RequestBody TransferenciaRecepcionParcialDTO recepcionParcialDTO) {
            transferService.receivePartialTransfer(id, recepcionParcialDTO);
            return ResponseEntity.ok(new MensajeDTO<>(false, "Operación exitosa"));
        }
    }





