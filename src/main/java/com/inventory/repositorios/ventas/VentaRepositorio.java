package com.inventory.repositorios.ventas;

import com.inventory.modelo.entidades.ventas.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {
    Page<Venta> findBySucursalId(Long sucursalId, Pageable pageable);
    Page<Venta> findByFechaVentaBetween(Date start, Date end, Pageable pageable);

    /** RF-24: Cuenta ventas del día actual. */
    @Query("SELECT COUNT(v) FROM Venta v WHERE DATE(v.fechaVenta) = CURRENT_DATE")
    long countVentasHoy();

    /** RF-24: Suma total de ingresos del día actual. */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE DATE(v.fechaVenta) = CURRENT_DATE")
    BigDecimal sumIngresoHoy();

    /** RF-29: Total de ventas por período. */
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :start AND :end")
    long countByPeriodo(@Param("start") Date start, @Param("end") Date end);

    /** RF-29: Suma de ingresos por período. */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :start AND :end")
    BigDecimal sumIngresoPeriodo(@Param("start") Date start, @Param("end") Date end);

    /** RF-29: Ventas agrupadas por sucursal en un período. */
    @Query("SELECT v.sucursalId, COUNT(v), COALESCE(SUM(v.total), 0) " +
           "FROM Venta v WHERE v.fechaVenta BETWEEN :start AND :end GROUP BY v.sucursalId")
    List<Object[]> findVentasPorSucursalYPeriodo(@Param("start") Date start, @Param("end") Date end);

    /** RF-31: Ventas agrupadas por mes y año para comparativas. */
    @Query(value = "SELECT MONTH(v.fecha_venta) as mes, COUNT(*) as total, COALESCE(SUM(v.total_venta), 0) as ingreso " +
                   "FROM ventas v WHERE YEAR(v.fecha_venta) = :anio GROUP BY MONTH(v.fecha_venta) ORDER BY mes",
           nativeQuery = true)
    List<Object[]> findVentasMensualesPorAnio(@Param("anio") int anio);
}
