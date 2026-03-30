package com.inventory.modelo.entidades.inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioId implements Serializable {
    private Long branchId;
    private Long productId;
}


