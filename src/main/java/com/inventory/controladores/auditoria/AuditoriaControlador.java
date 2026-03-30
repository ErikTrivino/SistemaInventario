    package com.inventory.controladores.auditoria;
    import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
    import org.springframework.web.bind.annotation.*;
import com.inventory.modelo.dto.comun.MensajeDTO;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;

    @RestController
    @RequestMapping("/api/auditoria")
    @RequiredArgsConstructor
    public class AuditoriaControlador {
        private final AuditoriaServicio auditService;

        @GetMapping
        public ResponseEntity<MensajeDTO<Object>> getAuditLogs() { return ResponseEntity.ok(new MensajeDTO<>(false, auditService.getAuditLogs())); }

        @GetMapping("/user/{id}")
        public ResponseEntity<MensajeDTO<Object>> getAuditLogsByUser(@PathVariable Long id) { return ResponseEntity.ok(new MensajeDTO<>(false, auditService.getAuditLogs())); // Need to add filter later
        }
    }





