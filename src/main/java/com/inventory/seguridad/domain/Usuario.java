package com.inventory.seguridad.domain;

import com.inventory.modelo.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa un usuario del sistema de inventario.
 *
 * El campo {@code email} es el identificador principal para autenticación
 * (subject del JWT), siguiendo el patrón de Back-EventosClick.
 * El campo {@code rol} determina qué prefijos de ruta puede acceder:
 *   - ADMIN    → /api/admin/**
 *   - MANAGER  → /api/manager/**
 *   - OPERATOR → /api/operator/**
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    /** Nombre completo del usuario. */
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /** Correo electrónico — identificador único para login y subject del JWT. */
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /** Contraseña hasheada con BCrypt. */
    @Column(name = "password_hash", nullable = false)
    private String password;

    /** Rol que determina el nivel de acceso a las rutas del API. */
    @Convert(converter = ConversorRol.class)
    @Column(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "id_sucursal_asignada")
    private Long sucursalAsignadaId;
}





