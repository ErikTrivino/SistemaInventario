    package com.inventory.repositorios.proveedores;
    import com.inventory.modelo.entidades.proveedores.ProductoProveedor;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface ProductoProveedorRepositorio extends JpaRepository<ProductoProveedor, Long> {
        List<ProductoProveedor> findBySupplierId(Long supplierId);
        List<ProductoProveedor> findByProductId(Long productId);
    }



