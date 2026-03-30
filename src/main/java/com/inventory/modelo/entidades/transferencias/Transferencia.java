package com.inventory.modelo.entidades.transferencias;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transferencia")
    private Long id;

    @Column(name = "id_sucursal_origen", nullable = false)
    private Long originBranchId;

    @Column(name = "id_sucursal_destino", nullable = false)
    private Long destinationBranchId;

    @Column(name = "id_producto", nullable = false)
    private Long productId;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal quantity;

    @Column(name = "id_usuario_solicita")
    private Long requestUserId;

    @Column(name = "id_gerente_aprueba")
    private Long approvedByManagerId;

    @Column(name = "estado", nullable = false, length = 30)
    private String status; // PENDING, APPROVED, SENT, RECEIVED

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "fecha_envio_estimada")
    private LocalDateTime estimatedDate;

    @Column(name = "fecha_recepcion_real")
    private LocalDateTime receivedDate;
}




