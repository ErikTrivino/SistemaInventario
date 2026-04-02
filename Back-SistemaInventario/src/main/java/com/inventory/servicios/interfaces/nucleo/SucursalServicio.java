package com.inventory.servicios.interfaces.nucleo;

import com.inventory.modelo.dto.nucleo.SucursalResponseDTO;
import java.util.List;

public interface SucursalServicio {
    List<SucursalResponseDTO> listarTodas();
    SucursalResponseDTO consultarPorId(Long id);
}
