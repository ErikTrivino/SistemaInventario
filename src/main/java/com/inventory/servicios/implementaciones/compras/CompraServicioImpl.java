package com.inventory.servicios.implementaciones.compras;

import com.inventory.servicios.interfaces.compras.CompraServicio;
import com.inventory.servicios.interfaces.inventario.InventarioServicio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import com.inventory.repositorios.compras.OrdenCompraRepositorio;
import com.inventory.repositorios.compras.DetalleCompraRepositorio;
import com.inventory.modelo.dto.compras.*;
import com.inventory.modelo.entidades.compras.OrdenCompra;
import com.inventory.modelo.entidades.compras.DetalleCompra;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class CompraServicioImpl implements CompraServicio {
    private final OrdenCompraRepositorio purchaseOrderRepository;
    private final DetalleCompraRepositorio purchaseDetailRepository;
    private final InventarioServicio inventoryService;
    private final AuditoriaServicio auditService;

    @Override
    @Transactional
    public CompraInformacionDTO createPurchase(OrdenCompraCrearDTO dto, Long userId) {
        if (dto.detalles() == null || dto.detalles().isEmpty()) {
            throw new RuntimeException("La orden debe tener al menos un producto.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompraCrearDTO item : dto.detalles()) {
            if (item.precioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("El precio unitario debe ser mayor a cero.");
            }
            if (item.descuentoPorcentaje() != null && (item.descuentoPorcentaje().compareTo(BigDecimal.ZERO) < 0 || item.descuentoPorcentaje().compareTo(new BigDecimal("100")) > 0)) {
                throw new RuntimeException("El descuento debe estar entre 0% y 100%.");
            }

            BigDecimal discountAmt = item.precioUnitario().multiply(item.cantidad())
                .multiply(item.descuentoPorcentaje() != null ? item.descuentoPorcentaje() : BigDecimal.ZERO)
                .divide(new BigDecimal("100"));
            
            BigDecimal subtotal = item.precioUnitario().multiply(item.cantidad()).subtract(discountAmt);
            total = total.add(subtotal);
        }

        OrdenCompra order = new OrdenCompra();
        order.setSucursalDestinoId(dto.idSucursalDestino());
        order.setProveedorId(dto.idProveedor());
        order.setUsuarioResponsableId(userId);
        order.setFechaCompra(LocalDateTime.now());
        order.setTotal(total);
        order.setEstado("Pendiente");
        order.setPlazoPagoDias(dto.plazoPagoDias());
        OrdenCompra saved = purchaseOrderRepository.save(order);

        for (DetalleCompraCrearDTO item : dto.detalles()) {
            DetalleCompra d = new DetalleCompra();
            d.setOrdenCompraId(saved.getId());
            d.setProductoId(item.idProducto());
            d.setCantidadSolicitada(item.cantidad());
            d.setCantidadRecibida(BigDecimal.ZERO);
            d.setPrecioUnitario(item.precioUnitario());
            d.setDescuentoAplicado(item.descuentoPorcentaje() != null ? item.descuentoPorcentaje() : BigDecimal.ZERO);
            purchaseDetailRepository.save(d);
        }

        auditService.registrarAccion(userId.toString(), "CREATE", "OrdenCompra", saved.getId(), "Created PO");
        return toInfo(saved);
    }

    @Override
    @Transactional
    public void receivePurchase(OrdenCompraRecepcionDTO dto) {
        OrdenCompra order = purchaseOrderRepository.findById(dto.idOrdenCompra())
                .orElseThrow(() -> new RuntimeException("PO not found"));

        if ("Recibido".equals(order.getEstado())) {
            throw new RuntimeException("La orden ya se encuentra recibida completamente.");
        }

        List<DetalleCompra> currentDetails = purchaseDetailRepository.findByOrdenCompraId(order.getId());

        for (DetalleRecepcionDTO recDto : dto.detallesRecibidos()) {
            DetalleCompra detalle = currentDetails.stream()
                .filter(d -> d.getId().equals(recDto.idDetalle()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Detalle no corresponde a la orden"));

            detalle.setCantidadRecibida(detalle.getCantidadRecibida().add(recDto.cantidadRecibida()));
            purchaseDetailRepository.save(detalle);

            if (recDto.cantidadRecibida().compareTo(BigDecimal.ZERO) > 0) {
                inventoryService.updateStock(
                    detalle.getProductoId(),
                    dto.idSucursalDestino(),
                    recDto.cantidadRecibida().doubleValue(),
                    "IN",
                    "Recepción de Orden Compra #" + order.getId(),
                    order.getUsuarioResponsableId() != null
                        ? order.getUsuarioResponsableId().toString()
                        : "sistema"
                );
            }
        }

        order.setEstado("Recibido");
        purchaseOrderRepository.save(order);
        auditService.registrarAccion("1", "RECEIVE", "OrdenCompra", order.getId(), "Received PO details and updated stock atomically.");
    }

    @Override
    public Page<CompraHistoricoRespuestaDTO> getPurchaseHistory(Long supplierId, Long productId, LocalDateTime start, LocalDateTime end, Integer pagina, Integer porPagina) {
        int pageNumber = (pagina != null) ? pagina : 0;
        int pageSize = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return purchaseDetailRepository.findHistoricalPurchases(supplierId, productId, start, end, pageable);
    }

    private CompraInformacionDTO toInfo(OrdenCompra purchaseOrder) {
        return new CompraInformacionDTO(
                purchaseOrder.getId(),
                purchaseOrder.getSucursalDestinoId(),
                purchaseOrder.getProveedorId(),
                purchaseOrder.getUsuarioResponsableId(),
                purchaseOrder.getFechaCompra(),
                purchaseOrder.getEstado(),
                purchaseOrder.getTotal()
        );
    }
}



