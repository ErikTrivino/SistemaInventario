package com.inventory.repositorios.ventas;

import com.inventory.modelo.entidades.ventas.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVentaId(Long ventaId);

    /**
     * RF-32: Análisis de rotación ABC — agrupa salidas por producto en un mes/año.
     * Ordena por mayor volumen de salidas para clasificar A (top 20%), B (20-50%), C (resto).
     */
    @Query(value = """
        SELECT dv.id_producto,
               SUM(dv.cantidad) as total_salidas,
               SUM(dv.cantidad * dv.precio_unitario * (1 - dv.descuento_aplicado / 100)) as valor_total
        FROM detalles_venta dv
        JOIN ventas v ON v.id_venta = dv.id_venta
        WHERE MONTH(v.fecha_venta) = :mes AND YEAR(v.fecha_venta) = :anio
        GROUP BY dv.id_producto
        ORDER BY total_salidas DESC
    """, nativeQuery = true)
    List<Object[]> findRotacionProductosPorMes(@Param("mes") int mes, @Param("anio") int anio);
}



