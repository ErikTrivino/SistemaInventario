package com.inventory.repositorios.proveedores;

import com.inventory.modelo.entidades.proveedores.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByRazonSocialContaining(String nombre);

    Optional<Proveedor> findByNitRut(String nitRut);

    Page<Proveedor> findByActivoTrue(Pageable pageable);
}
