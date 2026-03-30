package com.inventory.modelo.dto.autenticacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de inicio de sesión.
 * El usuario se identifica con su correo electrónico y contraseña,
 * siguiendo el mismo patrón que Back-EventosClick.
 */
public record LoginDTO(
        @NotBlank @Email String correo,
        @NotBlank String password
) {
}


