package com.inventory.modelo.entidades.proveedores;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;

    @Column(name = "nit_rut", nullable = false, unique = true, length = 20)
    private String nitRut;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "contacto", length = 100)
    private String contacto;

    /** RF-38: Correo del proveedor para comunicaciones. */
    @Column(name = "email", length = 120)
    private String email;

    /** RF-38: Estado lógico activo/inactivo del proveedor. */
    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}




