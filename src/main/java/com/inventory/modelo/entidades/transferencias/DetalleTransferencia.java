package com.inventory.modelo.entidades.transferencias;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Detalle de la transferencia: especifica qué producto se está moviendo y en qué cantidad.
 */
@Entity
@Table(name = "detalles_transferencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transferencia", nullable = false)
    private Transferencia transferencia;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_solicitada", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadSolicitada;

    /** Cantidad ajustada por el origen según disponibilidad física. */
    @Column(name = "cantidad_confirmada", precision = 12, scale = 2)
    private BigDecimal cantidadConfirmada;

    /** Cantidad efectivamente registrada al llegar al destino. */
    @Column(name = "cantidad_recibida", precision = 12, scale = 2)
    private BigDecimal cantidadRecibida;

    /** Justificación en caso de faltantes o daños detectados durante el envío. */
    @Column(name = "motivo_diferencia", columnDefinition = "TEXT")
    private String motivoDiferencia;
}
