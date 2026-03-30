    package com.inventory.repositorios.transferencias;
    import com.inventory.modelo.entidades.transferencias.Transferencia;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface TransferenciaRepositorio extends JpaRepository<Transferencia, Long> {
        List<Transferencia> findByOriginBranchId(Long branchId);
        List<Transferencia> findByDestinationBranchId(Long branchId);
        List<Transferencia> findByStatus(String status);
    }



