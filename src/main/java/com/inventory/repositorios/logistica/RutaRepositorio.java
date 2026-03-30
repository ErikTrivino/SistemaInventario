package com.inventory.repositorios.logistica;

import com.inventory.modelo.entidades.logistica.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepositorio extends JpaRepository<Ruta, Long> {
}



