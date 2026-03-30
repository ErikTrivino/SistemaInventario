package com.inventory.modelo.entidades.inventario;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unit_of_measures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}



