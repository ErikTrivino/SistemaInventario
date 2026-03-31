    package com.inventory.repositorios.compras;
    import com.inventory.modelo.entidades.compras.DetalleCompra;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    @Repository
    public interface DetalleCompraRepositorio extends JpaRepository<DetalleCompra, Long> {
        List<DetalleCompra> findByOrdenCompraId(Long ordenCompraId);
        
        @org.springframework.data.jpa.repository.Query("SELECT new com.inventory.modelo.dto.compras.CompraHistoricoRespuestaDTO(" +
               "o.id, p.id, p.nombre, prov.id, prov.razonSocial, d.cantidadSolicitada, d.cantidadRecibida, d.precioUnitario, o.fechaCompra" +
               ") FROM DetalleCompra d " +
               "JOIN OrdenCompra o ON d.ordenCompraId = o.id " +
               "JOIN Producto p ON d.productoId = p.id " +
               "JOIN Proveedor prov ON o.proveedorId = prov.id " +
               "WHERE (:proveedorId IS NULL OR prov.id = :proveedorId) " +
               "AND (:productoId IS NULL OR p.id = :productoId) " +
               "AND (:fechaInicio IS NULL OR o.fechaCompra >= :fechaInicio) " +
               "AND (:fechaFin IS NULL OR o.fechaCompra <= :fechaFin)")
        Page<com.inventory.modelo.dto.compras.CompraHistoricoRespuestaDTO> findHistoricalPurchases(
            @org.springframework.data.repository.query.Param("proveedorId") Long proveedorId,
            @org.springframework.data.repository.query.Param("productoId") Long productoId,
            @org.springframework.data.repository.query.Param("fechaInicio") java.time.LocalDateTime fechaInicio,
            @org.springframework.data.repository.query.Param("fechaFin") java.time.LocalDateTime fechaFin,
            Pageable pageable
        );
    }



