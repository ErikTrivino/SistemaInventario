package com.inventory.modelo.entidades.ventas;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(name = "id_sucursal", nullable = false)
    private Long sucursalId;

    @Column(name = "id_usuario_vendedor")
    private Long vendedorId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_venta", nullable = false)
    private java.util.Date fechaVenta;

    @Column(name = "total_venta", precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "comprobante_original", unique = true)
    private String comprobanteOriginal;
}




