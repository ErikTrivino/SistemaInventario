    package com.inventory.repositorios.proveedores;
    import com.inventory.modelo.entidades.proveedores.Proveedor;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {
        List<Proveedor> findByNameContaining(String name);
    }



