package com.inventory.servicios.implementaciones.nucleo;

import com.inventory.modelo.dto.nucleo.SucursalResponseDTO;
import com.inventory.modelo.entidades.nucleo.Sucursal;
import com.inventory.repositorios.nucleo.SucursalRepositorio;
import com.inventory.servicios.interfaces.nucleo.SucursalServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SucursalServicioImpl implements SucursalServicio {

    private final SucursalRepositorio sucursalRepositorio;

    @Override
    public List<SucursalResponseDTO> listarTodas() {
        return sucursalRepositorio.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SucursalResponseDTO consultarPorId(Long id) {
        return sucursalRepositorio.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
    }

    private SucursalResponseDTO toDTO(Sucursal s) {
        return new SucursalResponseDTO(
                s.getId(),
                s.getNombre(),
                s.getDireccion(),
                s.getCiudad(),
                s.getActivo()
        );
    }
}
