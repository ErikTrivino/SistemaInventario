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
    private String taxId;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String name;

    @Column(name = "contacto", length = 100)
    private String contact;
}




