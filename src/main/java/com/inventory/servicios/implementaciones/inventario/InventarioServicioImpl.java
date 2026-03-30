    package com.inventory.servicios.implementaciones.inventario;
    import com.inventory.servicios.interfaces.inventario.InventarioServicio;
    import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
    import com.inventory.eventos.PublicadorEventos;
    import com.inventory.repositorios.inventario.ProductoRepositorio;
    import com.inventory.repositorios.inventario.InventarioRepositorio;
    import com.inventory.repositorios.inventario.MovimientoInventarioRepositorio;
    import com.inventory.modelo.dto.inventario.InventarioInformacionDTO;
    import com.inventory.modelo.dto.inventario.ProductoCrearDTO;
    import com.inventory.modelo.dto.inventario.ProductoDetalleDTO;
    import com.inventory.modelo.dto.inventario.ProductoEditarDTO;
    import com.inventory.modelo.dto.inventario.ProductoInformacionDTO;
    import com.inventory.modelo.entidades.inventario.Producto;
    import com.inventory.modelo.entidades.inventario.Inventario;
    import com.inventory.modelo.entidades.inventario.MovimientoInventario;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import lombok.RequiredArgsConstructor;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class InventarioServicioImpl implements InventarioServicio {
        private final ProductoRepositorio productRepository;
        private final InventarioRepositorio inventoryRepository;
        private final MovimientoInventarioRepositorio movementRepository;
        private final AuditoriaServicio auditService;
        private final PublicadorEventos eventPublisher;

        @Override
        @Transactional
        public ProductoDetalleDTO createProduct(ProductoCrearDTO dto) {
            Producto product = Producto.builder()
                    .name(dto.nombre())
                    .description(dto.descripcion())
                    .sku(dto.sku())
                    .unitType(dto.unidadMedidaBase())
                    .averageCost(dto.precioCostoPromedio() == null ? java.math.BigDecimal.ZERO : dto.precioCostoPromedio())
                    .build();
            Producto saved = productRepository.save(product);
            auditService.logAction(1L, "CREATE", "Producto", saved.getId(), "Created product");
            return toDetalleDTO(saved);
        }

        @Override
        public ProductoDetalleDTO updateProduct(Long id, ProductoEditarDTO dto) {
            Producto product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            product.setName(dto.nombre());
            product.setDescription(dto.descripcion());
            product.setSku(dto.sku());
            product.setUnitType(dto.unidadMedidaBase());
            product.setAverageCost(dto.precioCostoPromedio() == null ? java.math.BigDecimal.ZERO : dto.precioCostoPromedio());

            Producto saved = productRepository.save(product);
            return toDetalleDTO(saved);
        }

        @Override
        public void deleteProduct(Long id) { }

        @Override
        public List<ProductoInformacionDTO> getProducts() {
            return productRepository.findAll().stream().map(this::toInformacionDTO).toList();
        }

        @Override
        public List<InventarioInformacionDTO> getInventoryByBranch(Long branchId) {
            return inventoryRepository.findByBranchId(branchId).stream().map(this::toInventarioInformacion).toList();
        }

        @Override
        @Transactional
        public void updateStock(Long productId, Long branchId, Double quantity, String type, String reason) {
            Inventario inv = inventoryRepository.findByProductIdAndBranchId(productId, branchId)
                    .orElseThrow(() -> new RuntimeException("Inventario not found"));

            java.math.BigDecimal amount = java.math.BigDecimal.valueOf(quantity);

            if (type.equals("OUT") && inv.getStock().compareTo(amount) < 0) {
                throw new RuntimeException("Insufficient stock");
            }

            if (type.equals("IN")) {
                inv.setStock(inv.getStock().add(amount));
            } else {
                inv.setStock(inv.getStock().subtract(amount));
            }
            inventoryRepository.save(inv);

            MovimientoInventario movement = new MovimientoInventario();
            // Set movement properties
            movementRepository.save(movement);

            if (inv.getStock().compareTo(inv.getMinStock()) < 0) {
                eventPublisher.publishStockUpdate(inv);
            }

            auditService.logAction(1L, "UPDATE_STOCK", "Inventario", inv.getProductId(), "Stock updated: " + quantity);
        }

        @Override
        public List<InventarioInformacionDTO> getLowStockProducts() {
            return inventoryRepository.findByQuantityLessThanMinStock().stream().map(this::toInventarioInformacion).toList();
        }

        private ProductoInformacionDTO toInformacionDTO(Producto product) {
            return new ProductoInformacionDTO(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    product.getUnitType(),
                    product.getAverageCost()
            );
        }

        private ProductoDetalleDTO toDetalleDTO(Producto product) {
            return new ProductoDetalleDTO(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getSku(),
                    product.getUnitType(),
                    product.getAverageCost()
            );
        }

        private InventarioInformacionDTO toInventarioInformacion(Inventario inventory) {
            return new InventarioInformacionDTO(
                    inventory.getBranchId(),
                    inventory.getProductId(),
                    inventory.getStock(),
                    inventory.getMinStock()
            );
        }
    }



