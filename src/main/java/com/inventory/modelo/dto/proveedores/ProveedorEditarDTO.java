package com.inventory.modelo.dto.proveedores;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorEditarDTO(
        @NotBlank @Size(max = 20) String nitRut,
        @NotBlank @Size(max = 150) String razonSocial,
        @Size(max = 100) String contacto
) {
}


