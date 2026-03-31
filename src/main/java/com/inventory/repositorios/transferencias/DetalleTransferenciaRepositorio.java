package com.inventory.repositorios.transferencias;

import com.inventory.modelo.entidades.transferencias.DetalleTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleTransferenciaRepositorio extends JpaRepository<DetalleTransferencia, Long> {
    List<DetalleTransferencia> findByTransferenciaId(Long transferenciaId);
}
