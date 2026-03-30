package com.inventory.modelo.entidades.proveedores;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoProveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}



