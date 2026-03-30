    package com.inventory.repositorios.auditoria;
    import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.Date;
    import java.util.List;

    @Repository
    public interface RegistroAuditoriaRepositorio extends JpaRepository<RegistroAuditoria, Long> {
        List<RegistroAuditoria> findByUserId(Long userId);
        List<RegistroAuditoria> findByEntity(String entity);
        List<RegistroAuditoria> findByCreatedAtBetween(Date start, Date end);
    }



