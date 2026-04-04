package com.inventory.modelo.dto.logistica;

/**
 * DTO para el seguimiento y comparación de tiempos de entrega.
 */
public record EnvioSeguimientoDTO(
    Long idEnvio,
    Long idTransferencia,
    Integer tiempoEstimado,
    Long tiempoRealDias,
    Long desviacionDias
) {}
