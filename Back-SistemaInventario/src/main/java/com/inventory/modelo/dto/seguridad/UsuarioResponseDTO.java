package com.inventory.modelo.dto.seguridad;

import com.inventory.modelo.enums.Rol;
import lombok.*;

/**
 * DTO para devolver la información detallada de un usuario sin exponer la contraseña.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private Rol rol;
    private Long sucursalAsignadaId;
    private Boolean activo;
    private String motivoInactivacion;
}
