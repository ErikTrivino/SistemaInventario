package com.inventory.modelo.dto.transferencias;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para la creación de una transferencia que incluye múltiples productos.
 */
public record TransferenciaCrearDTO(
        @NotNull Long idSucursalOrigen,
        @NotNull Long idSucursalDestino,
        @NotEmpty List<ItemTransferenciaDTO> items
) {}
