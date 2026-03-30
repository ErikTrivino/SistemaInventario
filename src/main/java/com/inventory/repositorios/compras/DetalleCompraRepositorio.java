    package com.inventory.repositorios.compras;
    import com.inventory.modelo.entidades.compras.DetalleCompra;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface DetalleCompraRepositorio extends JpaRepository<DetalleCompra, Long> {
        List<DetalleCompra> findByPurchaseId(Long purchaseId); // Assuming id points to OrdenCompra
    }



