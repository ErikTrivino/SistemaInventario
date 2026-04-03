package com.inventory.servicios.compras;

import com.inventory.modelo.dto.compras.*;
import com.inventory.modelo.entidades.compras.OrdenCompra;
import com.inventory.modelo.entidades.compras.DetalleCompra;
import com.inventory.repositorios.compras.DetalleCompraRepositorio;
import com.inventory.repositorios.compras.OrdenCompraRepositorio;
import com.inventory.servicios.implementaciones.compras.CompraServicioImpl;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import com.inventory.servicios.interfaces.inventario.InventarioServicio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompraServicioTest {

    @InjectMocks
    private CompraServicioImpl compraServicio;

    @Mock
    private OrdenCompraRepositorio purchaseOrderRepository;

    @Mock
    private DetalleCompraRepositorio purchaseDetailRepository;

    @Mock
    private InventarioServicio inventoryService;

    @Mock
    private AuditoriaServicio auditService;

    @Test
    void testCrearCompra() throws Exception {
        DetalleCompraCrearDTO detalle = new DetalleCompraCrearDTO(
                1L, // idProducto
                new BigDecimal("10.00"), // cantidad
                new BigDecimal("1500.00"), // precioUnitario
                new BigDecimal("0.00") // descuento
        );

        OrdenCompraCrearDTO dto = new OrdenCompraCrearDTO(
                1L, // idSucursalDestino
                1L, // idProveedor
                30, // plazoPagoDias
                List.of(detalle)
        );

        when(purchaseOrderRepository.save(any(OrdenCompra.class))).thenAnswer(invocation -> {
            OrdenCompra order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(1L);
            }
            return order;
        });

        CompraInformacionDTO resultado = compraServicio.crearCompra(dto, 1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.idCompra());
        assertEquals("Pendiente", resultado.estado());
        // Usar compareTo para BigDecimal
        assertEquals(0, new BigDecimal("15000.00").compareTo(resultado.total()));
    }

    @Test
    void testRecibirCompra() throws Exception {
        OrdenCompra mockOrder = new OrdenCompra();
        mockOrder.setId(1L);
        mockOrder.setEstado("Pendiente");

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(purchaseOrderRepository.save(any(OrdenCompra.class))).thenAnswer(i -> i.getArgument(0));

        DetalleCompra mockDetalle = new DetalleCompra();
        mockDetalle.setId(1L);
        mockDetalle.setProductoId(1L);
        mockDetalle.setCantidadSolicitada(new BigDecimal("10.00"));
        mockDetalle.setCantidadRecibida(BigDecimal.ZERO);
        mockDetalle.setOrdenCompraId(1L);

        when(purchaseDetailRepository.findByOrdenCompraId(1L)).thenReturn(List.of(mockDetalle));

        DetalleRecepcionDTO detalleRecibido = new DetalleRecepcionDTO(
                1L, // idDetalle (corresponde al mockDetalle)
                new BigDecimal("5.00") // cantidadRecibida
        );

        OrdenCompraRecepcionDTO dto = new OrdenCompraRecepcionDTO(
                1L, // idOrdenCompra
                1L, // idSucursalDestino
                List.of(detalleRecibido)
        );

        assertDoesNotThrow(() -> compraServicio.recibirCompra(dto));
    }

    @Test
    void testConsultarHistorial() throws Exception {
        Page<CompraHistoricoRespuestaDTO> mockPage = new PageImpl<>(List.of());
        when(purchaseDetailRepository.obtenerHistoricoCompras(any(), any(), any(), any(), any(), any())).thenReturn(mockPage);

        Page<CompraHistoricoRespuestaDTO> historial = compraServicio.obtenerHistoricoCompras(
                1L, null,1L, LocalDateTime.now().minusDays(30), LocalDateTime.now(), 0, 10
        );

        assertNotNull(historial);
        assertTrue(historial.getContent().isEmpty());
    }
}
