package com.inventory.modelo.entidades.inventario;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Id
    @Column(name = "id_sucursal", nullable = false)
    private Long sucursalId;

    @Builder.Default
    @Column(name = "stock_actual", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal stock = java.math.BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal stockMinimo = java.math.BigDecimal.ZERO;
}




