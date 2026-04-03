package com.inventory.modelo.dto.ventas;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record VentaCrearDTO(
        @NotNull Long idSucursal,
        @NotNull Long idResponsable,
        @NotEmpty List<DetalleVentaCrearDTO> detalles
) {}
