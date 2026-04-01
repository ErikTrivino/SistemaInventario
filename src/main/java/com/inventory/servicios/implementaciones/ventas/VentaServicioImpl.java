package com.inventory.servicios.implementaciones.ventas;

import com.inventory.servicios.interfaces.ventas.VentaServicio;
import com.inventory.servicios.interfaces.inventario.InventarioServicio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import com.inventory.eventos.PublicadorEventos;
import com.inventory.repositorios.ventas.VentaRepositorio;
import com.inventory.repositorios.ventas.DetalleVentaRepositorio;
import com.inventory.repositorios.inventario.InventarioRepositorio;
import com.inventory.modelo.dto.ventas.*;
import com.inventory.modelo.entidades.ventas.Venta;
import com.inventory.modelo.entidades.ventas.DetalleVenta;
import com.inventory.modelo.entidades.inventario.Inventario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Date;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class VentaServicioImpl implements VentaServicio {
    private final VentaRepositorio saleRepository;
    private final DetalleVentaRepositorio detailRepository;
    private final InventarioServicio inventoryService;
    private final InventarioRepositorio inventoryRepository;
    private final AuditoriaServicio auditService;
    private final PublicadorEventos eventPublisher;

    @Override
    public ValidacionStockDTO validateStock(Long productId, Long branchId, BigDecimal quantity) {
        Inventario inv = inventoryRepository.findByProducto_IdAndSucursal_Id(productId, branchId).orElse(null);
        BigDecimal stock = inv != null ? inv.getStock() : BigDecimal.ZERO;
        
        return new ValidacionStockDTO(
                productId,
                quantity,
                stock,
                stock.compareTo(quantity) >= 0
        );
    }

    @Override
    @Transactional
    public VentaInformacionDTO createSale(VentaCrearDTO dto, Long userId) {
        if (dto.detalles() == null || dto.detalles().isEmpty()) {
            throw new RuntimeException("La venta debe contener al menos un producto.");
        }

        BigDecimal totalVenta = BigDecimal.ZERO;
        
        for (DetalleVentaCrearDTO item : dto.detalles()) {
            ValidacionStockDTO val = validateStock(item.idProducto(), dto.idSucursal(), item.cantidad());
            if (!val.disponible()) {
                throw new RuntimeException("Stock insuficiente para el producto ID: " + item.idProducto());
            }

            if (item.descuentoPorcentaje() != null && 
               (item.descuentoPorcentaje().compareTo(BigDecimal.ZERO) < 0 || item.descuentoPorcentaje().compareTo(new BigDecimal("100")) > 0)) {
                throw new RuntimeException("El descuento no puede ser negativo ni mayor a 100%.");
            }

            BigDecimal rDesc = item.descuentoPorcentaje() != null ? item.descuentoPorcentaje() : BigDecimal.ZERO;
            BigDecimal amtDiscount = item.precioUnitario().multiply(item.cantidad())
                    .multiply(rDesc).divide(new BigDecimal("100"));
            
            BigDecimal finalPrice = item.precioUnitario().multiply(item.cantidad()).subtract(amtDiscount);
            totalVenta = totalVenta.add(finalPrice);
        }

        Venta sale = Venta.builder()
                .sucursalId(dto.idSucursal())
                .vendedorId(userId)
                .fechaVenta(new Date())
                .total(totalVenta)
                .comprobanteOriginal(UUID.randomUUID().toString()) // Generador Dummy
                .build();
        Venta saved = saleRepository.save(sale);

        for (DetalleVentaCrearDTO item : dto.detalles()) {
            inventoryService.updateStock(
                item.idProducto(), 
                dto.idSucursal(), 
                item.cantidad().doubleValue(), 
                "OUT", 
                "Venta #" + saved.getId(),
                userId.toString()
            );

            DetalleVenta d = DetalleVenta.builder()
                .ventaId(saved.getId())
                .productoId(item.idProducto())
                .cantidad(item.cantidad())
                .precioUnitario(item.precioUnitario())
                .descuentoAplicado(item.descuentoPorcentaje() != null ? item.descuentoPorcentaje() : BigDecimal.ZERO)
                .listaPrecioUsada(item.listaPrecioUsada())
                .build();
            detailRepository.save(d);
        }

        eventPublisher.publicarVentaCompletada(saved, userId.toString());
        auditService.registrarAccion(userId.toString(), "CREATE", "Venta", saved.getId(), "Comercialización procesada");
        
        return toInfo(saved);
    }

    @Override
    public Page<VentaInformacionDTO> getSalesByBranch(Long branchId, Integer pagina, Integer porPagina) {
        int pageNumber = (pagina != null) ? pagina : 0;
        int pageSize = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return saleRepository.findBySucursalId(branchId, pageable).map(this::toInfo);
    }

    @Override
    public Page<VentaInformacionDTO> getSalesByDateRange(Date start, Date end, Integer pagina, Integer porPagina) {
        int pageNumber = (pagina != null) ? pagina : 0;
        int pageSize = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return saleRepository.findByFechaVentaBetween(start, end, pageable).map(this::toInfo);
    }

    private VentaInformacionDTO toInfo(Venta sale) {
        return new VentaInformacionDTO(
                sale.getId(),
                sale.getSucursalId(),
                sale.getVendedorId(),
                sale.getFechaVenta(),
                sale.getTotal(),
                sale.getComprobanteOriginal()
        );
    }
}



