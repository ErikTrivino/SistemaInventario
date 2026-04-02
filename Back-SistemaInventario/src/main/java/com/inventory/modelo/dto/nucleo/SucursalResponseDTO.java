package com.inventory.modelo.dto.nucleo;

public record SucursalResponseDTO(
    Long id,
    String nombre,
    String direccion,
    String ciudad,
    boolean activo
) {}
