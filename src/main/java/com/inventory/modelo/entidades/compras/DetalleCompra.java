package com.inventory.modelo.entidades.compras;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @Column(name = "id_orden_compra", nullable = false)
    private Long ordenCompraId;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal cantidadSolicitada;

    @Column(name = "cantidad_recibida", precision = 12, scale = 2)
    private java.math.BigDecimal cantidadRecibida;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal precioUnitario;

    @Column(name = "descuento_aplicado", precision = 5, scale = 2)
    @Builder.Default
    private java.math.BigDecimal descuentoAplicado = java.math.BigDecimal.ZERO;
}



