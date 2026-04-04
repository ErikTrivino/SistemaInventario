package com.inventory.modelo.dto.inventario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductoCrearDTO(
                @NotBlank @Size(max = 150) String nombre,
                String descripcion,
                @Size(max = 50) String sku,
                @NotBlank @Size(max = 20) String unidadMedidaBase,

                boolean activo,
                Long idProveedor,
                List<DetalleProdcutoCrearDTO> detalleProdcutoCrearDTO) {
}
