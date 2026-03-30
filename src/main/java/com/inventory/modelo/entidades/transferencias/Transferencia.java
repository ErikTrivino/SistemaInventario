package com.inventory.modelo.entidades.transferencias;

import com.inventory.modelo.entidades.logistica.Envio;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que gestiona el movimiento de mercancía entre sucursales.
 * Ahora desacoplada para soportar múltiples productos y seguimiento logístico.
 */
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

    @Column(name = "id_usuario_solicita")
    private Long usuarioSolicitaId;

    @Column(name = "id_gerente_aprueba")
    private Long gerenteApruebaId;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // PENDING, APPROVED, SENT, RECEIVED

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    /** Lista de productos incluidos en esta transferencia. */
    @Builder.Default
    @OneToMany(mappedBy = "transferencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleTransferencia> detalles = new ArrayList<>();

    /** Información logística asociada al envío de esta transferencia. */
    @OneToOne(mappedBy = "transferencia", cascade = CascadeType.ALL)
    private Envio envio;
}
