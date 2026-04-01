package com.inventory.repositorios.eventos;

import com.inventory.modelo.entidades.eventos.NotificacionEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionEventoRepositorio extends JpaRepository<NotificacionEvento, Long> {
}
