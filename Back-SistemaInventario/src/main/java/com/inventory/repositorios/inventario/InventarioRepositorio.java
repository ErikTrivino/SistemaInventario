package com.inventory.repositorios.inventario;

import com.inventory.modelo.entidades.inventario.Inventario;
import com.inventory.modelo.entidades.inventario.InventarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface InventarioRepositorio extends JpaRepository<Inventario, InventarioId> {

    Optional<Inventario> findByProducto_IdAndSucursal_Id(Long productoId, Long sucursalId);

    @Modifying
    @Transactional
    @Query("UPDATE Inventario i SET i.activo = :activo WHERE i.producto.id = :productoId AND i.sucursal.id = :sucursalId")
    void updateActivoStatus(@Param("productoId") Long productoId, @Param("sucursalId") Long sucursalId, @Param("activo") Boolean activo);

    org.springframework.data.domain.Page<Inventario> findBySucursal_Id(Long sucursalId, org.springframework.data.domain.Pageable pageable);

    /** RF-29: Buscar productos con stock bajo el mínimo. */
    @Query("SELECT i FROM Inventario i WHERE i.stock < i.stockMinimo")
    org.springframework.data.domain.Page<Inventario> findByQuantityLessThanMinStock(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT SUM(i.stock) FROM Inventario i WHERE i.producto.id = :productoId")
    java.math.BigDecimal sumStockByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT new com.inventory.modelo.dto.inventario.InventarioRespuestaDTO(" +
            "i.producto.id, i.producto.nombre, i.producto.sku, i.producto.unidadMedidaBase, i.producto.descripcion, i.activo, " +
            "i.sucursal.id, i.stock, i.stockMinimo, i.producto.precioCostoPromedio, null) " +
            "FROM Inventario i " +
            "WHERE i.sucursal.id = :sucursalId AND (:activo IS NULL OR i.activo = :activo)")
    org.springframework.data.domain.Page<com.inventory.modelo.dto.inventario.InventarioRespuestaDTO> findCatalogByBranch(@Param("sucursalId") Long sucursalId, @Param("activo") Boolean activo, org.springframework.data.domain.Pageable pageable);
    @Query("SELECT new com.inventory.modelo.dto.inventario.ProductoDetallePorSucursalDTO(" +
           "i.producto.id, i.producto.nombre, i.producto.descripcion, i.producto.sku, i.producto.unidadMedidaBase, i.producto.precioCostoPromedio, " +
           "i.stock, i.activo, i.sucursal.id, " +
           "(SELECT MIN(pp.proveedorId) FROM ProductoProveedor pp WHERE pp.productoId = i.producto.id)) " +
           "FROM Inventario i " +
           "WHERE i.sucursal.id = :sucursalId AND i.producto.id = :productoId")
    Optional<com.inventory.modelo.dto.inventario.ProductoDetallePorSucursalDTO> findProductDetailByBranchAndProduct(@Param("sucursalId") Long sucursalId, @Param("productoId") Long productoId);
}
