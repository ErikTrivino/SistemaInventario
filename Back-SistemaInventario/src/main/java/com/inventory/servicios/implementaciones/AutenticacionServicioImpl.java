package com.inventory.servicios.implementaciones;

import com.inventory.config.JWTUtils;
import com.inventory.modelo.dto.autenticacion.LoginDTO;
import com.inventory.modelo.dto.autenticacion.RegistroUsuarioDTO;
import com.inventory.modelo.dto.autenticacion.TokenDTO;
import com.inventory.modelo.entidades.seguridad.Usuario;
import com.inventory.repositorios.seguridad.UsuarioRepositorio;
import com.inventory.servicios.interfaces.AutenticacionServicio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import lombok.RequiredArgsConstructor;
import com.inventory.config.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de autenticación del sistema de inventario.
 *
 * Sigue el mismo patrón que CuentaServicioImpl de Back-EventosClick:
 *   - Login con BCrypt para verificar contraseña.
 *   - Generación de token JWT con claims {rol, nombre, id}.
 *   - Registro de nuevos usuarios con contraseña hasheada.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AutenticacionServicioImpl implements AutenticacionServicio {

    private final UsuarioRepositorio userRepository;
    private final JWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaServicio auditService;

    /**
     * Valida las credenciales y genera un token JWT si son correctas.
     * El token incluye los claims: rol, nombre e id del usuario,
     * y el subject del token es el email del usuario.
     */
    @Override
    public TokenDTO iniciarSesion(LoginDTO loginDTO) throws Exception {
        // Buscar usuario por correo electrónico
        Usuario user = obtenerPorEmail(loginDTO.correo());

        // Verificar contraseña con BCrypt
        if (!passwordEncoder.matches(loginDTO.password(), user.getContrasena())) {
            throw new Exception("La contraseña es incorrecta");
        }

        // Construir los claims del token (igual que EventosClick)
        Map<String, Object> claims = construirClaims(user);

        // Generar y retornar el token JWT
        TokenDTO token = new TokenDTO(jwtUtils.generarToken(user.getCorreo(), claims));
        
        auditService.registrarAccion(user.getId().toString(), "LOGIN", "Usuario", user.getId(), "Sesión iniciada correctamente");
        
        return token;
    }

    /**
     * Registra un nuevo usuario en el sistema de inventario.
     * Verifica que el correo no esté ya registrado y hashea la contraseña.
     */
    @Override
    public String registrarUsuario(RegistroUsuarioDTO dto) throws Exception {
        // Verificar que el correo no esté ya registrado
        if (userRepository.findByCorreo(dto.correo()).isPresent()) {
            throw new Exception("Ya existe un usuario registrado con el correo " + dto.correo());
        }

        // Crear el nuevo usuario
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(dto.nombre())
                .correo(dto.correo())
                .contrasena(passwordEncoder.encode(dto.password()))
                .rol(dto.rol())
                .build();

        Usuario saved = userRepository.save(nuevoUsuario);

        auditService.registrarAccion("SISTEMA", "REGISTER_USER", "Usuario", saved.getId(), "Usuario registrado exitosamente con rol " + dto.rol().name());

        return "Usuario registrado exitosamente con rol " + dto.rol().name();
    }


    // ── Métodos privados auxiliares ────────────────────────────────────────────

    /**
     * Obtiene un usuario por su correo, lanzando excepción si no existe.
     */
    private Usuario obtenerPorEmail(String email) throws Exception {
        Optional<Usuario> userOptional = userRepository.findByCorreo(email);

        if (userOptional.isEmpty()) {
            throw new Exception("No existe un usuario registrado con el correo " + email);
        }

        return userOptional.get();
    }

    /**
     * Construye el mapa de claims que se incluirá en el token JWT.
     * Mismo patrón que construirClaims() de CuentaServicioImpl en EventosClick.
     */
    private Map<String, Object> construirClaims(Usuario user) {
        return Map.of(
                "rol", user.getRol(),
                "nombre", user.getNombre(),
                "id", user.getId()
        );
    }
}


