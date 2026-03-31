package com.inventory.repositorios.inventario;

import com.inventory.modelo.entidades.inventario.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);

    List<Producto> findByNombreContaining(String nombre);
}
