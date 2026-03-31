package com.inventory.servicios.implementaciones.auditoria;

import com.inventory.modelo.entidades.auditoria.RegistroAuditoria;
import com.inventory.repositorios.auditoria.RegistroAuditoriaRepositorio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    public Page<RegistroAuditoria> obtenerLogs(Integer pagina, Integer porPagina) {
        int pageNumber = (pagina != null) ? pagina : 0;
        int pageSize = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return repository.findAll(pageable);
    }

    @Override
    public Page<RegistroAuditoria> obtenerLogsPorUsuario(String usuarioId, Integer pagina, Integer porPagina) {
        int pageNumber = (pagina != null) ? pagina : 0;
        int pageSize = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return repository.findByUsuario(usuarioId, pageable);
    }
}
