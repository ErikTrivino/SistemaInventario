package com.inventory.modelo.entidades.transferencias;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalles_transferencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}



