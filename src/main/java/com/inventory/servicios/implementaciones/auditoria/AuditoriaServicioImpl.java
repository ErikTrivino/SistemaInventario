package com.inventory.servicios.implementaciones.auditoria;

import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
import com.inventory.repositorios.auditoria.RegistroAuditoriaRepositorio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServicioImpl implements AuditoriaServicio {

    private final RegistroAuditoriaRepositorio repository;

    @Override
    public void registrarAccion(String usuario, String accion, String entidad, Long entidadId, String detalles) {
        RegistroAuditoria registro = RegistroAuditoria.builder()
                .usuario(usuario)
                .accion(accion)
                .entidad(entidad)
                .fechaHora(LocalDateTime.now())
                .detalles(detalles + " (ID entidad: " + entidadId + ")")
                .build();
        repository.save(registro);
    }

    @Override
    public List<RegistroAuditoria> obtenerLogs() {
        return repository.findAll();
    }
}
