package com.inventory.modelo.entidades.inventario;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;
    
    // IN or OUT
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipo;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal cantidad;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "id_usuario_responsable")
    private Long usuarioId;

    @Column(name = "id_sucursal", nullable = false)
    private Long sucursalId;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;
}




