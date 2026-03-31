    package com.inventory.repositorios.transferencias;
    import com.inventory.modelo.entidades.transferencias.Transferencia;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;

    @Repository
    public interface TransferenciaRepositorio extends JpaRepository<Transferencia, Long> {
        List<Transferencia> findBySucursalOrigenId(Long sucursalId);
        List<Transferencia> findBySucursalDestinoId(Long sucursalId);
        List<Transferencia> findByEstado(String estado);
        
        @org.springframework.data.jpa.repository.Query("SELECT t FROM Transferencia t " +
               "WHERE (:sucursalId IS NULL OR t.sucursalOrigenId = :sucursalId OR t.sucursalDestinoId = :sucursalId) " +
               "AND (:estado IS NULL OR t.estado = :estado) " +
               "AND (:fechaInicio IS NULL OR t.fechaSolicitud >= :fechaInicio) " +
               "AND (:fechaFin IS NULL OR t.fechaSolicitud <= :fechaFin)")
        Page<Transferencia> findHistoricalTransfers(
            @org.springframework.data.repository.query.Param("sucursalId") Long sucursalId,
            @org.springframework.data.repository.query.Param("estado") String estado,
            @org.springframework.data.repository.query.Param("fechaInicio") java.time.LocalDateTime fechaInicio,
            @org.springframework.data.repository.query.Param("fechaFin") java.time.LocalDateTime fechaFin,
            Pageable pageable
        );
    }



