package com.inventory.modelo.dto.autenticacion;

/**
 * DTO de respuesta que contiene el token JWT generado tras un inicio de sesión exitoso.
 * Patrón idéntico al TokenDTO de Back-EventosClick.
 */
public record TokenDTO(
        String token
) {
}


