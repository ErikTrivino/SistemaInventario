package com.inventory.servicios.implementaciones.seguridad;

import com.inventory.modelo.dto.seguridad.UsuarioRequestDTO;
import com.inventory.modelo.dto.seguridad.UsuarioResponseDTO;
import com.inventory.modelo.entidades.seguridad.Usuario;
import com.inventory.modelo.enums.Rol;
import com.inventory.repositorios.seguridad.UsuarioRepositorio;
import com.inventory.servicios.interfaces.seguridad.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import com.inventory.config.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de usuarios.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        if (usuarioRepositorio.findByCorreo(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .correo(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .sucursalAsignadaId(request.getSucursalAsignadaId())
                .activo(true)
                .build();

        return mapToDTO(usuarioRepositorio.save(usuario));
    }

    @Override
    public UsuarioResponseDTO consultarPorId(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        return mapToDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO consultarPorEmail(String email) {
        Usuario usuario = usuarioRepositorio.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        return mapToDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (request.getNombre() != null) usuario.setNombre(request.getNombre());
        if (request.getApellido() != null) usuario.setApellido(request.getApellido());
        if (request.getRol() != null) usuario.setRol(request.getRol());
        if (request.getSucursalAsignadaId() != null) usuario.setSucursalAsignadaId(request.getSucursalAsignadaId());
        if (request.getActivo() != null) usuario.setActivo(request.getActivo());

        return mapToDTO(usuarioRepositorio.save(usuario));
    }

    @Override
    @Transactional
    public void inactivarUsuario(Long id, String motivo) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        usuario.setActivo(false);
        usuario.setMotivoInactivacion(motivo);
        usuarioRepositorio.save(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> filtrarPorSucursal(Long sucursalId) {
        return usuarioRepositorio.findBySucursalAsignadaId(sucursalId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponseDTO> filtrarPorRol(Rol rol) {
        return usuarioRepositorio.findByRol(rol).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponseDTO> buscarPorNombre(String query, Boolean activo) {
        List<Usuario> usuarios;
        if (query != null && !query.isEmpty()) {
            usuarios = usuarioRepositorio.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(query, query);
        } else {
            usuarios = usuarioRepositorio.findAll();
        }

        if (activo != null) {
            usuarios = usuarios.stream().filter(u -> u.getActivo().equals(activo)).collect(Collectors.toList());
        }

        return usuarios.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public String cambiarRol(Long id, Rol nuevoRol) throws Exception {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
        usuario.setRol(nuevoRol);
        usuarioRepositorio.save(usuario);
        return "Rol del usuario " + usuario.getNombre() + " actualizado a " + nuevoRol.name();
    }

    @Override
    public org.springframework.data.domain.Page<UsuarioResponseDTO> obtenerUsuarios(org.springframework.data.domain.Pageable pageable) {
        return usuarioRepositorio.findAll(pageable).map(this::mapToDTO);
    }


    private UsuarioResponseDTO mapToDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .sucursalAsignadaId(usuario.getSucursalAsignadaId())
                .activo(usuario.getActivo())
                .motivoInactivacion(usuario.getMotivoInactivacion())
                .build();
    }
}
