    package com.inventory.servicios.implementaciones.compras;
    import com.inventory.servicios.interfaces.compras.CompraServicio;
    import com.inventory.servicios.interfaces.inventario.InventarioServicio;
    import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
    import com.inventory.repositorios.compras.OrdenCompraRepositorio;
    import com.inventory.modelo.dto.compras.CompraCrearDTO;
    import com.inventory.modelo.dto.compras.CompraInformacionDTO;
    import com.inventory.modelo.entidades.compras.OrdenCompra;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import lombok.RequiredArgsConstructor;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class CompraServicioImpl implements CompraServicio {
        private final OrdenCompraRepositorio purchaseOrderRepository;
        private final InventarioServicio inventoryService;
        private final AuditoriaServicio auditService;

        @Override
        @Transactional
        public CompraInformacionDTO createPurchase(CompraCrearDTO dto) {
            OrdenCompra order = new OrdenCompra();
            order.setBranchId(dto.idSucursalDestino());
            order.setSupplierId(dto.idProveedor());
            order.setResponsibleUserId(dto.idUsuarioResponsable());
            order.setPurchaseDate(java.time.LocalDateTime.now());
            order.setTotal(dto.total());
            order.setStatus("Pendiente");
            OrdenCompra saved = purchaseOrderRepository.save(order);
            auditService.logAction(1L, "CREATE", "OrdenCompra", saved.getId(), "Created PO");
            return toInfo(saved);
        }

        @Override
        @Transactional
        public void receivePurchase(Long purchaseId) {
            OrdenCompra order = purchaseOrderRepository.findById(purchaseId)
                    .orElseThrow(() -> new RuntimeException("PO not found"));

            order.setStatus("Recibido");
            purchaseOrderRepository.save(order);

            // actualiza inventario (IN), recalcula costo promedio
            // loop details and inventoryService.updateStock(...)

            auditService.logAction(1L, "RECEIVE", "OrdenCompra", order.getId(), "Received PO");
        }

        @Override
        public List<CompraInformacionDTO> getPurchasesBySupplier(Long supplierId) {
            return purchaseOrderRepository.findBySupplierId(supplierId).stream().map(this::toInfo).toList();
        }

        private CompraInformacionDTO toInfo(OrdenCompra purchaseOrder) {
            return new CompraInformacionDTO(
                    purchaseOrder.getId(),
                    purchaseOrder.getBranchId(),
                    purchaseOrder.getSupplierId(),
                    purchaseOrder.getResponsibleUserId(),
                    purchaseOrder.getPurchaseDate(),
                    purchaseOrder.getStatus(),
                    purchaseOrder.getTotal()
            );
        }
    }



