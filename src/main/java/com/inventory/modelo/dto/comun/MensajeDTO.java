package com.inventory.modelo.dto.comun;

public record MensajeDTO<T>(
        boolean error,
        T respuesta
) {
}


