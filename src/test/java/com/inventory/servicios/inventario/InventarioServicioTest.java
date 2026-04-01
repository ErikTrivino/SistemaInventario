package com.inventory.servicios.inventario;

import com.inventory.modelo.dto.inventario.InventarioInformacionDTO;
import com.inventory.modelo.dto.inventario.ProductoCrearDTO;
import com.inventory.modelo.dto.inventario.ProductoDetalleDTO;

import com.inventory.modelo.entidades.inventario.Producto;
import com.inventory.modelo.entidades.inventario.Inventario;
import com.inventory.repositorios.inventario.InventarioRepositorio;
import com.inventory.repositorios.inventario.MovimientoInventarioRepositorio;
import com.inventory.repositorios.inventario.ProductoRepositorio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import com.inventory.servicios.implementaciones.inventario.InventarioServicioImpl;
import com.inventory.eventos.PublicadorEventos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventarioServicioTest {

    @InjectMocks
    private InventarioServicioImpl inventarioServicio;

    @Mock
    private ProductoRepositorio productRepository;

    @Mock
    private InventarioRepositorio inventoryRepository;

    @Mock
    private MovimientoInventarioRepositorio movementRepository;

    @Mock
    private AuditoriaServicio auditService;

    @Mock
    private PublicadorEventos eventPublisher;

    @Test
    void testCrearProducto() throws Exception {
        ProductoCrearDTO dto = new ProductoCrearDTO(
                "Producto Prueba",
                "Descripción de prueba",
                "SKU-PROB-001",
                "Unidad",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                1L // idSucursal
        );

        when(productRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto product = invocation.getArgument(0);
            if (product.getId() == null) {
                product.setId(1L);
            }
            return product;
        });

        ProductoDetalleDTO resultado = inventarioServicio.createProduct(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.idProducto());
    }

    @Test
    void testActualizarStock() throws Exception {
        Long productId = 1L;
        Long branchId = 1L;

        Inventario mockInv = new Inventario();
        mockInv.setStock(new BigDecimal("100.00"));
        mockInv.setStockMinimo(new BigDecimal("10.00"));
        
        when(inventoryRepository.findByProducto_IdAndSucursal_Id(productId, branchId))
                .thenReturn(Optional.of(mockInv));

        assertDoesNotThrow(() -> 
            inventarioServicio.updateStock(productId, branchId, 10.0, "IN", "Ajuste de prueba", "test-usuario"));
    }

    @Test
    void testConsultarInventarioPorSucursal() throws Exception {
        when(inventoryRepository.findBySucursal_Id(any(Long.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        Page<InventarioInformacionDTO> inventario = inventarioServicio.getInventoryByBranch(1L, 1, 10);
        assertNotNull(inventario);
    }
}
