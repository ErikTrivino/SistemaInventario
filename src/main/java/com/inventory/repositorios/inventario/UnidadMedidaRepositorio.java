package com.inventory.repositorios.inventario;

import com.inventory.modelo.entidades.inventario.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadMedidaRepositorio extends JpaRepository<UnidadMedida, Long> {
    java.util.List<UnidadMedida> findByProductoId(Long productoId);
}



