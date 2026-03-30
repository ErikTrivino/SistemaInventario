package com.inventory.seguridad.dto;

import com.inventory.modelo.enums.Rol;
import lombok.*;

/**
 * DTO para capturar los datos de creación o actualización de un usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private Rol rol;
    private Long sucursalAsignadaId;
    private Boolean activo;
    private String motivoInactivacion;
}
