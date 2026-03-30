package com.inventory.modelo.entidades.ventas;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @Column(name = "id_venta", nullable = false)
    private Long ventaId;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal precioUnitario;

    @Column(name = "descuento_aplicado", precision = 5, scale = 2)
    @Builder.Default
    private java.math.BigDecimal descuentoAplicado = java.math.BigDecimal.ZERO;

    @Column(name = "lista_precio_usada", length = 50)
    private String listaPrecioUsada;
}
