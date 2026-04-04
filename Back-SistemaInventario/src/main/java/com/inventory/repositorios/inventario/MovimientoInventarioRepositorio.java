package com.inventory.repositorios.inventario;

import com.inventory.modelo.dto.inventario.InformacionMovimientoDTO;
import com.inventory.modelo.entidades.inventario.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Long> {
    @Query("""
        SELECT new com.inventory.modelo.dto.inventario.InformacionMovimientoDTO(
            m.id, m.tipo, m.cantidad, m.fechaMovimiento, 
            m.usuarioId, COALESCE(u.nombre, 'Sistema'),
            m.sucursalId, s.nombre,
            m.productoId, p.nombre,
            m.referenciaId, m.motivo
        )
        FROM MovimientoInventario m
        LEFT JOIN Producto p ON m.productoId = p.id
        LEFT JOIN Sucursal s ON m.sucursalId = s.id
        LEFT JOIN Usuario u ON m.usuarioId = u.id
        WHERE (:productoId IS NULL OR m.productoId = :productoId)
        AND (:sucursalId IS NULL OR m.sucursalId = :sucursalId)
        AND (cast(:fechaInicio as timestamp) IS NULL OR m.fechaMovimiento >= :fechaInicio)
        AND (cast(:fechaFin as timestamp) IS NULL OR m.fechaMovimiento <= :fechaFin)
        """)
    Page<InformacionMovimientoDTO> buscarMovimientos(
            @Param("productoId") Long productoId,
            @Param("sucursalId") Long sucursalId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}
