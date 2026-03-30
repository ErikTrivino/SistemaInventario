package com.inventory.modelo.entidades.logistica;

import com.inventory.modelo.enums.ClasificacionRuta;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que parametriza y optimiza los traslados recurrentes entre nodos de la red.
 */
@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long id;

    @Column(name = "id_sucursal_origen", nullable = false)
    private Long sucursalOrigenId;

    @Column(name = "id_sucursal_destino", nullable = false)
    private Long sucursalDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_clasificacion", nullable = false, length = 30)
    private ClasificacionRuta tipoClasificacion;

    /** Tiempo base esperado para esa ruta en horas. */
    @Column(name = "lead_time_estandar", nullable = false)
    private Integer leadTimeEstandar;
}
