package com.inventory.servicios.implementaciones.inventario;

import com.inventory.servicios.interfaces.inventario.UnidadMedidaServicio;
import com.inventory.repositorios.inventario.UnidadMedidaRepositorio;
import com.inventory.modelo.dto.inventario.UnidadMedidaCrearDTO;
import com.inventory.modelo.entidades.inventario.UnidadMedida;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadMedidaServicioImpl implements UnidadMedidaServicio {
    private final UnidadMedidaRepositorio repository;

    @Override
    public UnidadMedida create(UnidadMedidaCrearDTO dto) {
        UnidadMedida um = new UnidadMedida();
        um.setNombre(dto.nombre());
        um.setAbreviatura(dto.abreviatura());
        um.setProductoId(dto.idProducto());
        um.setEsUnidadBase(dto.esUnidadBase() != null ? dto.esUnidadBase() : false);
        um.setFactorConversion(dto.factorConversion());
        return repository.save(um);
    }
    
    @Override
    public List<UnidadMedida> getByProductId(Long productId) {
        return repository.findByProductoId(productId);
    }

    @Override
    public List<UnidadMedida> getAll() {
        return repository.findAll();
    }
}
