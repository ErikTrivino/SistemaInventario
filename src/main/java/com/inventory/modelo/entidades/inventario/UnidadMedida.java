package com.inventory.modelo.entidades.inventario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidades_medida")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidad_medida")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "abreviatura", nullable = false, length = 20)
    private String abbreviation;

    @Column(name = "id_producto")
    private Long productId;

    @Column(name = "es_unidad_base", nullable = false)
    @Builder.Default
    private Boolean isBaseUnit = false;

    @Column(name = "factor_conversion", nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private java.math.BigDecimal conversionFactor = java.math.BigDecimal.ONE;
}
