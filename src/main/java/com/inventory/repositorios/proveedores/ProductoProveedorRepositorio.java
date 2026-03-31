package com.inventory.repositorios.proveedores;

import com.inventory.modelo.dto.proveedores.CumplimientoProveedorDTO;
import com.inventory.modelo.entidades.proveedores.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductoProveedorRepositorio extends JpaRepository<ProductoProveedor, Long> {
    Page<ProductoProveedor> findByProveedorId(Long proveedorId, Pageable pageable);
    Page<ProductoProveedor> findByProductoId(Long productoId, Pageable pageable);

    /**
     * RF-41/RF-42: KPI de cumplimiento de proveedor.
     * Compara si las recepciones de los pedidos se hicieron dentro del lead time pactado.
     */
    @Query(value = """
        SELECT new com.inventory.modelo.dto.proveedores.CumplimientoProveedorDTO(
            :supplierId,
            COUNT(oc.id),
            SUM(CASE WHEN DATEDIFF(dr.fechaCompra, oc.fechaCompra) <= pp.leadTimeDias THEN 1 ELSE 0 END),
            SUM(CASE WHEN DATEDIFF(dr.fechaCompra, oc.fechaCompra) > pp.leadTimeDias THEN 1 ELSE 0 END),
            CASE WHEN COUNT(oc.id) > 0
                 THEN (SUM(CASE WHEN DATEDIFF(dr.fechaCompra, oc.fechaCompra) <= pp.leadTimeDias THEN 1.0 ELSE 0.0 END) / COUNT(oc.id)) * 100
                 ELSE 0.0
            END
        )
        FROM OrdenCompra oc
        JOIN DetalleCompra dc ON dc.ordenCompraId = oc.id
        JOIN ProductoProveedor pp ON pp.proveedorId = oc.proveedorId AND pp.productoId = dc.productoId
        LEFT JOIN OrdenCompra dr ON dr.id = oc.id
        WHERE oc.proveedorId = :supplierId AND oc.estado = 'Recibido'
    """, nativeQuery = false)
    CumplimientoProveedorDTO calcularCumplimientoProveedor(@Param("supplierId") Long supplierId);
}
