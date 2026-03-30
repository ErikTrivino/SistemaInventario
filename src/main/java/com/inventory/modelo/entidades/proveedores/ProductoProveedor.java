package com.inventory.modelo.entidades.proveedores;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos_proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_proveedor")
    private Long id;

    /** ID del proveedor. */
    @Column(name = "id_proveedor", nullable = false)
    private Long proveedorId;

    /** ID del producto que provee. */
    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    /** RF-39: Precio de compra pactado con el proveedor. */
    @Column(name = "precio_compra", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal precioCompra;

    /** RF-39: Descuento negociado con el proveedor (0-100%). */
    @Builder.Default
    @Column(name = "descuento_proveedor", precision = 5, scale = 2)
    private java.math.BigDecimal descuentoProveedor = java.math.BigDecimal.ZERO;

    /** RF-39: Tiempo estimado de entrega en días (Lead Time). */
    @Column(name = "lead_time_dias", nullable = false)
    private Integer leadTimeDias;

    /** Fecha desde la que aplica este precio. */
    @Column(name = "fecha_vigencia_desde")
    private java.time.LocalDate fechaVigenciaDesde;
}



