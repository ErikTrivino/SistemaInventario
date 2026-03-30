package com.inventory.seguridad.controller;

import com.inventory.modelo.dto.comun.MensajeDTO;
import com.inventory.modelo.enums.Rol;
import com.inventory.seguridad.dto.UsuarioRequestDTO;
import com.inventory.seguridad.dto.UsuarioResponseDTO;
import com.inventory.seguridad.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de usuarios (RF-53 a RF-60).
 * Accesible bajo el prefijo /api/admin para cumplir con las reglas de RBAC.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<MensajeDTO<UsuarioResponseDTO>> crearUsuario(@RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<UsuarioResponseDTO>> consultarPorId(@PathVariable Long id) {
        UsuarioResponseDTO response = usuarioService.consultarPorId(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<MensajeDTO<UsuarioResponseDTO>> consultarPorEmail(@PathVariable String email) {
        UsuarioResponseDTO response = usuarioService.consultarPorEmail(email);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO<UsuarioResponseDTO>> actualizarUsuario(
            @PathVariable Long id, @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @PatchMapping("/{id}/inactivar")
    public ResponseEntity<MensajeDTO<String>> inactivarUsuario(
            @PathVariable Long id, @RequestParam String motivo) {
        usuarioService.inactivarUsuario(id, motivo);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Usuario inactivado correctamente."));
    }

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<MensajeDTO<List<UsuarioResponseDTO>>> filtrarPorSucursal(@PathVariable Long sucursalId) {
        List<UsuarioResponseDTO> response = usuarioService.filtrarPorSucursal(sucursalId);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<MensajeDTO<List<UsuarioResponseDTO>>> filtrarPorRol(@PathVariable Rol rol) {
        List<UsuarioResponseDTO> response = usuarioService.filtrarPorRol(rol);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/busqueda")
    public ResponseEntity<MensajeDTO<List<UsuarioResponseDTO>>> buscarPorNombre(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean activo) {
        List<UsuarioResponseDTO> response = usuarioService.buscarPorNombre(query, activo);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }
}
