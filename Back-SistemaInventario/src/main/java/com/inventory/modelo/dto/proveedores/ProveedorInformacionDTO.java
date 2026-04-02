package com.inventory.modelo.dto.proveedores;

public record ProveedorInformacionDTO(
        Long id,
        String nitRut,
        String razonSocial,
        String contacto,
        String email,
        boolean activo
) {}


