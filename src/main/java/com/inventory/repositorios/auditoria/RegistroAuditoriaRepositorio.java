    package com.inventory.repositorios.auditoria;
    import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface RegistroAuditoriaRepositorio extends JpaRepository<RegistroAuditoria, Long> {
        java.util.List<RegistroAuditoria> findByUsuario(String usuario);
        java.util.List<RegistroAuditoria> findByEntidad(String entidad);
        java.util.List<RegistroAuditoria> findByFechaHoraBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    }



