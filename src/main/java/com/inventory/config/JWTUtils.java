package com.inventory.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * Utilidad para la generación y validación de tokens JWT (JSON Web Token).
 *
 * Esta clase se encarga de:
 *   - Generar tokens JWT firmados con una clave secreta.
 *   - Validar y analizar tokens existentes para obtener sus claims (información contenida).
 *
 * Se usa principalmente en el proceso de autenticación y autorización
 * de usuarios dentro del sistema de inventario.
 */
@Component
public class JWTUtils {

    /**
     * Genera un token JWT con un conjunto de claims (atributos) y un tiempo de expiración de 1 hora.
     *
     * @param email  correo electrónico del usuario, que se establece como "subject" del token
     * @param claims mapa de atributos adicionales (por ejemplo, rol, nombre, id)
     * @return una cadena con el token JWT firmado
     */
    public String generarToken(String email, Map<String, Object> claims) {

        // Instante actual (momento de emisión)
        Instant now = Instant.now();

        // Se construye el token con los datos proporcionados
        return Jwts.builder()
                .claims(claims)                                         // información personalizada
                .subject(email)                                         // identificador principal del token
                .issuedAt(Date.from(now))                               // fecha de emisión
                .expiration(Date.from(now.plus(1L, ChronoUnit.HOURS))) // expiración en 1 hora
                .signWith(getKey())                                     // firma del token con la clave secreta
                .compact();                                             // genera el token final en formato String
    }

    /**
     * Analiza y valida un token JWT, verificando su firma e integridad.
     *
     * @param jwtString token JWT a validar
     * @return un objeto {@link Jws} que contiene los claims del token si la validación es exitosa
     * @throws ExpiredJwtException      si el token ha expirado
     * @throws UnsupportedJwtException  si el token usa un formato no soportado
     * @throws MalformedJwtException    si el token tiene un formato incorrecto
     * @throws IllegalArgumentException si el token está vacío o es nulo
     */
    public Jws<Claims> parseJwt(String jwtString)
            throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, IllegalArgumentException {

        // Crea el parser con la clave secreta y valida la firma del token
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(getKey())
                .build();

        // Retorna los claims (payload) del token si es válido
        return jwtParser.parseSignedClaims(jwtString);
    }

    /**
     * Genera y retorna la clave secreta utilizada para firmar y verificar los tokens JWT.
     *
     * ⚠️ En producción, esta clave debe almacenarse de forma segura
     * (variable de entorno o gestor de secretos) y no estar en el código fuente.
     *
     * @return una instancia de {@link SecretKey} derivada de la clave secreta definida
     */
    private SecretKey getKey() {
        // Clave secreta usada para firmar los tokens (HMAC-SHA)
        String claveSecreta = "inventariosecretinventariosecretinventariosecret";

        // Convierte la clave en bytes y genera la clave criptográfica
        byte[] secretKeyBytes = claveSecreta.getBytes();

        // Retorna la clave en formato compatible con el algoritmo HMAC-SHA
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }
}


