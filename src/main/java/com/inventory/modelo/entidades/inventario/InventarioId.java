package com.inventory.modelo.entidades.inventario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clase para la llave compuesta de inventario_sucursal.
 * Los nombres de los campos deben coincidir con los nombres de los atributos @Id en la entidad Inventario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioId implements Serializable {
    private Long sucursal;
    private Long producto;
}
