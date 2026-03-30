package com.inventory.seguridad.repository;

import com.inventory.seguridad.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     * El email es el identificador principal de login (subject del JWT).
     */
    Optional<Usuario> findByEmail(String email);
}





