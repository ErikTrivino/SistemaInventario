    package com.inventory.servicios.implementaciones.auditoria;
    import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
    import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class AuditoriaServicioImpl implements AuditoriaServicio {
        @Override public void logAction(Long userId, String action, String entity, Long entityId, String changes) { }
        @Override public List<RegistroAuditoria> getAuditLogs() { return null; }
    }



