package com.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.modelo.dto.comun.MensajeDTO;
import com.inventory.modelo.enums.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro personalizado que intercepta todas las solicitudes HTTP
 * para validar el token JWT antes de permitir el acceso a los recursos protegidos.
 *
 * Este filtro:
 *   - Configura las cabeceras CORS necesarias.
 *   - Extrae y valida el token JWT enviado en el encabezado {@code Authorization}.
 *   - Permite o bloquea el acceso según el rol del usuario y la ruta solicitada.
 *
 * Extiende {@link OncePerRequestFilter}, por lo que se ejecuta una sola vez
 * por cada petición HTTP.
 */
@Component
@RequiredArgsConstructor
public class FiltroToken extends OncePerRequestFilter {

    /** Utilidad para manejo y validación de tokens JWT. */
    private final JWTUtils jwtUtils;

    /**
     * Método principal del filtro: se ejecuta antes de procesar cada solicitud.
     *
     * Rutas protegidas:
     *   /api/admin/**    → requiere rol ADMIN
     *   /api/manager/**  → requiere rol MANAGER
     *   /api/operator/** → requiere rol OPERATOR
     *   /api/auth/**     → público (sin validación de token)
     *
     * @param request     petición HTTP entrante
     * @param response    respuesta HTTP saliente
     * @param filterChain cadena de filtros para continuar el flujo de ejecución
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // --- Configuración de cabeceras CORS ---
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Accept, Content-Type, Authorization");

        // Las solicitudes OPTIONS (preflight) se responden sin validar el token
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {

            // URI del recurso solicitado
            String requestURI = request.getRequestURI();

            // Token JWT obtenido del encabezado Authorization
            String token = getToken(request);
            boolean error = true;

            try {
                // Validación de permisos según la ruta y el rol
                if (requestURI.startsWith("/api/admin")) {
                    error = validarToken(token, Rol.ADMIN);
                } else if (requestURI.startsWith("/api/manager")) {
                    error = validarToken(token, Rol.MANAGER);
                } else if (requestURI.startsWith("/api/operator")) {
                    error = validarToken(token, Rol.OPERATOR);
                } else {
                    // Rutas públicas: /api/auth/**, /api/public/**, etc.
                    error = false;
                }

                // Si hay error de permisos, se devuelve respuesta 403
                if (error) {
                    crearRespuestaError("No tiene permisos para acceder a este recurso",
                            HttpServletResponse.SC_FORBIDDEN, response);
                }

            } catch (MalformedJwtException | SignatureException e) {
                crearRespuestaError("El token es incorrecto",
                        HttpServletResponse.SC_UNAUTHORIZED, response);
            } catch (ExpiredJwtException e) {
                crearRespuestaError("El token está vencido",
                        HttpServletResponse.SC_UNAUTHORIZED, response);
            } catch (Exception e) {
                crearRespuestaError(e.getMessage(),
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
            }

            // Si no hay errores, se continúa con el flujo normal del request
            if (!error) {
                filterChain.doFilter(request, response);
            }
        }
    }

    /**
     * Extrae el token JWT del encabezado "Authorization" de la solicitud.
     *
     * @param req petición HTTP
     * @return el token JWT sin el prefijo "Bearer ", o {@code null} si no existe
     */
    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ")
                ? header.replace("Bearer ", "")
                : null;
    }

    /**
     * Crea y envía una respuesta JSON con un mensaje de error personalizado.
     *
     * @param mensaje    mensaje descriptivo del error
     * @param codigoError código HTTP que se devolverá
     * @param response   objeto de respuesta HTTP
     */
    private void crearRespuestaError(String mensaje, int codigoError, HttpServletResponse response) throws IOException {
        MensajeDTO<String> dto = new MensajeDTO<>(true, mensaje);

        response.setContentType("application/json");
        response.setStatus(codigoError);
        response.getWriter().write(new ObjectMapper().writeValueAsString(dto));
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * Valida un token JWT verificando que sea válido y que el rol coincida con el esperado.
     *
     * @param token token JWT a validar
     * @param rol   rol esperado (ADMIN, MANAGER u OPERATOR)
     * @return {@code true} si hay error o el token no es válido; {@code false} si el token es correcto
     */
    private boolean validarToken(String token, Rol rol) {
        boolean error = true;

        if (token != null) {
            // Se analiza y valida el token JWT
            Jws<Claims> jws = jwtUtils.parseJwt(token);

            // Verifica que el rol contenido en el token coincida con el rol esperado
            if (Rol.valueOf(jws.getPayload().get("rol").toString()) == rol) {
                error = false;
            }
        }

        return error;
    }
}


