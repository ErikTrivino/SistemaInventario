    package com.inventory.servicios.implementaciones.ventas;
    import com.inventory.servicios.interfaces.ventas.VentaServicio;
    import com.inventory.servicios.interfaces.inventario.InventarioServicio;
    import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
    import com.inventory.eventos.PublicadorEventos;
    import com.inventory.repositorios.ventas.VentaRepositorio;
    import com.inventory.modelo.dto.ventas.VentaCrearDTO;
    import com.inventory.modelo.dto.ventas.VentaInformacionDTO;
    import com.inventory.modelo.entidades.ventas.Venta;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import lombok.RequiredArgsConstructor;
    import java.util.Date;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class VentaServicioImpl implements VentaServicio {
        private final VentaRepositorio saleRepository;
        private final InventarioServicio inventoryService;
        private final AuditoriaServicio auditService;
        private final PublicadorEventos eventPublisher;

        @Override
        @Transactional
        public VentaInformacionDTO createSale(VentaCrearDTO dto) {
            // valida stock disponible
            // inventoryService.updateStock(..., "OUT", "SALE");

            Venta sale = Venta.builder()
                    .branchId(dto.idSucursal())
                    .sellerUserId(dto.idUsuarioVendedor())
                    .createdAt(new Date())
                    .total(dto.totalVenta())
                    .build();
            Venta saved = saleRepository.save(sale);

            eventPublisher.publishSale(saved);
            auditService.logAction(1L, "CREATE", "Venta", saved.getId(), "Created Venta");
            return toInfo(saved);
        }

        @Override
        public List<VentaInformacionDTO> getSalesByBranch(Long branchId) {
            return saleRepository.findByBranchId(branchId).stream().map(this::toInfo).toList();
        }

        @Override
        public List<VentaInformacionDTO> getSalesByDateRange(Date start, Date end) {
            return saleRepository.findByCreatedAtBetween(start, end).stream().map(this::toInfo).toList();
        }

        private VentaInformacionDTO toInfo(Venta sale) {
            return new VentaInformacionDTO(
                    sale.getId(),
                    sale.getBranchId(),
                    sale.getSellerUserId(),
                    sale.getCreatedAt(),
                    sale.getTotal()
            );
        }
    }



