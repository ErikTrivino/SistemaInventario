package com.inventory.modelo.dto.autenticacion;

import com.inventory.modelo.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

/**
 * DTO para registrar un nuevo usuario en el sistema de inventario.
 * Incluye nombre, correo, contraseña y el rol que se asignará.
 */
public record RegistroUsuarioDTO(
        @NotBlank @Length(max = 100) String nombre,
        @NotBlank @Email @Length(max = 100) String correo,
        @NotBlank @Length(min = 7, max = 20) String password,
        @NotNull Rol rol
) {
}


