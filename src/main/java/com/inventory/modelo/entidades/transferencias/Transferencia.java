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
    private Long sucursalOrigenId;

    @Column(name = "id_sucursal_destino", nullable = false)
    private Long sucursalDestinoId;

    @Column(name = "id_producto", nullable = false)
    private Long productoId;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal cantidad;

    @Column(name = "id_usuario_solicita")
    private Long usuarioSolicitaId;

    @Column(name = "id_gerente_aprueba")
    private Long gerenteApruebaId;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // PENDING, APPROVED, SENT, RECEIVED

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_envio_estimada")
    private LocalDateTime fechaEnvioEstimada;

    @Column(name = "fecha_recepcion_real")
    private LocalDateTime fechaRecepcionReal;

    @Column(name = "cantidad_confirmada", precision = 12, scale = 2)
    private java.math.BigDecimal cantidadConfirmada;

    @Column(name = "cantidad_recibida", precision = 12, scale = 2)
    private java.math.BigDecimal cantidadRecibida;
}




