package com.inventory.seguridad;

import com.inventory.config.FiltroToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad del sistema de inventario.
 *
 * La validación real de tokens JWT y roles se delega completamente
 * a {@link FiltroToken}, siguiendo el patrón de Back-EventosClick.
 * Spring Security se configura en modo stateless con todas las rutas
 * permitidas a nivel framework; el control de acceso lo hace FiltroToken.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final FiltroToken filtroToken;

    public ConfiguracionSeguridad(FiltroToken filtroToken) {
        this.filtroToken = filtroToken;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // FiltroToken maneja la autorización por prefijo de ruta + rol en claims
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Registrar FiltroToken antes del filtro de autenticación estándar
                .addFilterBefore(filtroToken, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encoder de contraseñas BCrypt para hashear passwords al registrar/actualizar usuarios.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}




