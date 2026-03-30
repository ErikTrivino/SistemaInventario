package com.inventory.modelo.entidades.compras;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ordenes_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long id;

    @Column(name = "id_sucursal_destino")
    private Long sucursalDestinoId;

    @Column(name = "id_proveedor", nullable = false)
    private Long proveedorId;

    @Column(name = "id_usuario_responsable")
    private Long usuarioResponsableId;

    @Column(name = "fecha_compra", nullable = false)
    private java.time.LocalDateTime fechaCompra;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "plazo_pago_dias")
    private Integer plazoPagoDias;
}





