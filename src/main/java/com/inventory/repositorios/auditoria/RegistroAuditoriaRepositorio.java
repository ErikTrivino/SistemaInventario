    package com.inventory.repositorios.auditoria;
    import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;

    @Repository
    public interface RegistroAuditoriaRepositorio extends JpaRepository<RegistroAuditoria, Long> {
        Page<RegistroAuditoria> findByUsuario(String usuario, Pageable pageable);
        java.util.List<RegistroAuditoria> findByEntidad(String entidad);
        java.util.List<RegistroAuditoria> findByFechaHoraBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    }



