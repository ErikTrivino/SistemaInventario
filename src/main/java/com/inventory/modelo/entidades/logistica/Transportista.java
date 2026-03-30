package com.inventory.modelo.entidades.logistica;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa una empresa transportista para la logística de envíos.
 */
@Entity
@Table(name = "transportistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transportista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "contacto", length = 150)
    private String contacto;

    @Column(name = "nit", unique = true, length = 20)
    private String nit;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
