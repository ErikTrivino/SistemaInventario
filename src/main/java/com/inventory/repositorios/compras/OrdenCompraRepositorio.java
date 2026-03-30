    package com.inventory.repositorios.compras;
    import com.inventory.modelo.entidades.compras.OrdenCompra;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface OrdenCompraRepositorio extends JpaRepository<OrdenCompra, Long> {
        List<OrdenCompra> findBySupplierId(Long supplierId);
        List<OrdenCompra> findByBranchId(Long branchId);
        List<OrdenCompra> findByStatus(String status);
    }



