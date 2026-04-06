package com.inventory.controladores.seguridad;

import com.inventory.modelo.dto.comun.MensajeDTO;
import com.inventory.modelo.dto.seguridad.UsuarioResponseDTO;
import com.inventory.modelo.enums.Rol;
import com.inventory.servicios.interfaces.seguridad.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la consulta de usuarios accesible por todos los roles.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioConsultaControlador {

    private final UsuarioServicio usuarioServicio;

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<UsuarioResponseDTO>> consultarPorId(@PathVariable Long id) {
        UsuarioResponseDTO response = usuarioServicio.consultarPorId(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<MensajeDTO<UsuarioResponseDTO>> consultarPorEmail(@PathVariable String email) {
        UsuarioResponseDTO response = usuarioServicio.consultarPorEmail(email);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<MensajeDTO<List<UsuarioResponseDTO>>> filtrarPorSucursal(@PathVariable Long sucursalId) {
        List<UsuarioResponseDTO> response = usuarioServicio.filtrarPorSucursal(sucursalId);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<MensajeDTO<List<UsuarioResponseDTO>>> filtrarPorRol(@PathVariable Rol rol) {
        List<UsuarioResponseDTO> response = usuarioServicio.filtrarPorRol(rol);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping("/busqueda")
    public ResponseEntity<MensajeDTO<List<UsuarioResponseDTO>>> buscarPorNombre(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean activo) {
        List<UsuarioResponseDTO> response = usuarioServicio.buscarPorNombre(query, activo);
        return ResponseEntity.ok(new MensajeDTO<>(false, response));
    }

    @GetMapping
    public ResponseEntity<MensajeDTO<Page<UsuarioResponseDTO>>> obtenerUsuarios(
            @RequestParam(required = false, defaultValue = "10") Integer porPagina,
            @RequestParam(required = false, defaultValue = "1") Integer pagina
    ) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(numPagina, tamanoPagina);
        Page<UsuarioResponseDTO> usuarios = usuarioServicio.obtenerUsuarios(pageable);
        return ResponseEntity.ok(new MensajeDTO<>(false, usuarios));
    }
}
