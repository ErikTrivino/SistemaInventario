package com.inventory.modelo.entidades.logistica;

import com.inventory.modelo.entidades.transferencias.Transferencia;
import com.inventory.modelo.enums.EstadoLogistico;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que rastrea el estado físico y logístico de una mercancía una vez despachada.
 */
@Entity
@Table(name = "envios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transferencia", nullable = false, unique = true)
    private Transferencia transferencia;

    @Column(name = "id_ruta")
    private Long rutaId;

    @Column(name = "id_transportista")
    private Long transportistaId;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    /** Tiempo base esperado para la entrega (ej. en horas o días). */
    @Column(name = "tiempo_estimado_entrega")
    private Integer tiempoEstimadoEntrega;

    @Column(name = "fecha_recepcion_real")
    private LocalDateTime fechaRecepcionReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_logistico", nullable = false, length = 30)
    private EstadoLogistico estado;
}
