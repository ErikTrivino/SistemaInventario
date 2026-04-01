package com.inventory.repositorios.inventario;

import com.inventory.modelo.entidades.inventario.Inventario;
import com.inventory.modelo.entidades.inventario.InventarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepositorio extends JpaRepository<Inventario, InventarioId> {

    Optional<Inventario> findByProducto_IdAndSucursal_Id(Long productoId, Long sucursalId);

    org.springframework.data.domain.Page<Inventario> findBySucursal_Id(Long sucursalId, org.springframework.data.domain.Pageable pageable);

    /** RF-29: Buscar productos con stock bajo el mínimo. */
    @Query("SELECT i FROM Inventario i WHERE i.stock < i.stockMinimo")
    org.springframework.data.domain.Page<Inventario> findByQuantityLessThanMinStock(org.springframework.data.domain.Pageable pageable);

    /** Búsqueda de catálogo activo por sucursal. */
    @Query("SELECT new com.inventory.modelo.dto.inventario.InventarioRespuestaDTO(" +
           "i.producto.id, i.producto.nombre, i.producto.sku, i.producto.unidadMedidaBase, i.producto.activo, " +
           "i.sucursal.id, i.stock, i.stockMinimo) " +
           "FROM Inventario i " +
           "WHERE i.sucursal.id = :sucursalId AND i.producto.activo = true")
    org.springframework.data.domain.Page<com.inventory.modelo.dto.inventario.InventarioRespuestaDTO> findActiveCatalogByBranch(@Param("sucursalId") Long sucursalId, org.springframework.data.domain.Pageable pageable);
}
