package com.inventory.modelo.entidades.inventario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sku", unique = true, length = 50)
    private String sku;
    
    @Column(name = "unidad_medida_base", nullable = false, length = 20)
    private String unitType;

    @Builder.Default
    @Column(name = "precio_costo_promedio", nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal averageCost = java.math.BigDecimal.ZERO;
}




