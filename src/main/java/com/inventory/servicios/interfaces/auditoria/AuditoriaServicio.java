    package com.inventory.servicios.interfaces.auditoria;
    import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
    import java.util.List;

    public interface AuditoriaServicio {
        void logAction(Long userId, String action, String entity, Long entityId, String changes);
        List<RegistroAuditoria> getAuditLogs();
    }



