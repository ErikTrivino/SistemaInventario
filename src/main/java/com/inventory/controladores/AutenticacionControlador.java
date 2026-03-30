package com.inventory.controladores;

import com.inventory.modelo.dto.autenticacion.LoginDTO;
import com.inventory.modelo.dto.autenticacion.RegistroUsuarioDTO;
import com.inventory.modelo.dto.autenticacion.TokenDTO;
import com.inventory.modelo.dto.comun.MensajeDTO;
import com.inventory.servicios.interfaces.AutenticacionServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que maneja las operaciones de autenticación del sistema de inventario.
 * Todas las rutas bajo /api/auth son públicas (no requieren token JWT).
 *
 * Patrón idéntico al AutenticacionControlador de Back-EventosClick.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    /**
     * Inicia sesión en el sistema con las credenciales del usuario.
     * Si son correctas, devuelve un token JWT para autenticación.
     *
     * POST /api/auth/iniciar-sesion
     *
     * @param loginDTO objeto con correo y contraseña
     * @return token JWT dentro de un MensajeDTO
     */
    @PostMapping("/iniciar-sesion")
    public ResponseEntity<MensajeDTO<TokenDTO>> iniciarSesion(
            @Valid @RequestBody LoginDTO loginDTO) throws Exception {
        TokenDTO token = autenticacionServicio.iniciarSesion(loginDTO);
        return ResponseEntity.ok(new MensajeDTO<>(false, token));
    }

    /**
     * Registra un nuevo usuario en el sistema de inventario.
     *
     * POST /api/auth/registrar
     *
     * @param dto datos del nuevo usuario (nombre, correo, password, rol)
     * @return mensaje de confirmación
     */
    @PostMapping("/registrar")
    public ResponseEntity<MensajeDTO<String>> registrarUsuario(
            @Valid @RequestBody RegistroUsuarioDTO dto) throws Exception {
        String mensaje = autenticacionServicio.registrarUsuario(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, mensaje));
    }
}


