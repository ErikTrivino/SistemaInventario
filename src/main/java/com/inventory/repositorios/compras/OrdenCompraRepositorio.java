    package com.inventory.repositorios.compras;
    import com.inventory.modelo.entidades.compras.OrdenCompra;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface OrdenCompraRepositorio extends JpaRepository<OrdenCompra, Long> {
        List<OrdenCompra> findByProveedorId(Long proveedorId);
        List<OrdenCompra> findBySucursalDestinoId(Long sucursalDestinoId);
        List<OrdenCompra> findByEstado(String estado);
    }



