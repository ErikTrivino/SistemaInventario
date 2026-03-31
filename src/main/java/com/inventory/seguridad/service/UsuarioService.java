package com.inventory.seguridad.service;

import com.inventory.modelo.enums.Rol;
import com.inventory.seguridad.domain.Usuario;
import com.inventory.seguridad.dto.UsuarioRequestDTO;
import com.inventory.seguridad.dto.UsuarioResponseDTO;
import com.inventory.seguridad.repository.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

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

    public UsuarioResponseDTO consultarPorId(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        return mapToDTO(usuario);
    }

    public UsuarioResponseDTO consultarPorEmail(String email) {
        Usuario usuario = usuarioRepositorio.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        return mapToDTO(usuario);
    }

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

    @Transactional
    public void inactivarUsuario(Long id, String motivo) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
        usuario.setActivo(false);
        usuario.setMotivoInactivacion(motivo);
        usuarioRepositorio.save(usuario);
    }

    public List<UsuarioResponseDTO> filtrarPorSucursal(Long sucursalId) {
        return usuarioRepositorio.findBySucursalAsignadaId(sucursalId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UsuarioResponseDTO> filtrarPorRol(Rol rol) {
        return usuarioRepositorio.findByRol(rol).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

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

    private UsuarioResponseDTO mapToDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getCorreo())
                .rol(usuario.getRol())
                .sucursalAsignadaId(usuario.getSucursalAsignadaId())
                .activo(usuario.getActivo())
                .motivoInactivacion(usuario.getMotivoInactivacion())
                .build();
    }
}
