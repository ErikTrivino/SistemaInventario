package com.inventory.servicios.interfaces.auditoria;

import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
import java.util.List;

/**
 * Servicio para el registro de auditoría de acciones en el sistema.
 */
public interface AuditoriaServicio {
    /**
     * Registra una acción de auditoría.
     * @param usuario Nombre o ID del usuario que realiza la acción.
     * @param accion Acción realizada (CREAR, ACTUALIZAR, ELIMINAR, etc.).
     * @param entidad Nombre de la entidad afectada.
     * @param entidadId ID de la instancia de la entidad afectada.
     * @param detalles Descripción de los cambios o detalles adicionales.
     */
    void registrarAccion(String usuario, String accion, String entidad, Long entidadId, String detalles);
    
    /**
     * Obtiene todos los logs de auditoría.
     */
    List<RegistroAuditoria> obtenerLogs();
}
