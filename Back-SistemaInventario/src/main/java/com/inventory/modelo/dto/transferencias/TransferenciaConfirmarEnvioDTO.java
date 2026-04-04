package com.inventory.modelo.dto.transferencias;

import jakarta.validation.constraints.NotNull;

public record TransferenciaConfirmarEnvioDTO(
        @NotNull Long idTransferencia,
        Integer tiempoEstimadoEntrega
) {}
