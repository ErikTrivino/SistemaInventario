package com.inventory.modelo.entidades.inventario;

import com.inventory.modelo.entidades.nucleo.Sucursal;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa la cantidad de un producto en una sucursal específica.
 * Usa una llave compuesta (Sucursal + Producto).
 */
@Entity
@Table(name = "inventario_sucursal")
@IdClass(InventarioId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventario {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Builder.Default
    @Column(name = "stock_actual", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal stock = java.math.BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal stockMinimo = java.math.BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "precio_costo_promedio", nullable = false, precision = 14, scale = 4)
    private java.math.BigDecimal precioCostoPromedio = java.math.BigDecimal.ZERO;

    // Helper methods for migration/compatibility
    public Long getSucursalId() {
        return sucursal != null ? sucursal.getId() : null;
    }

    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
    }
}
