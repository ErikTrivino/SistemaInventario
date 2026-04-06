package com.inventory.servicios.implementaciones.inventario;

import com.inventory.modelo.dto.inventario.*;
import lombok.extern.slf4j.Slf4j;
import com.inventory.servicios.interfaces.inventario.InventarioServicio;
import com.inventory.eventos.PublicadorEventos;
import com.inventory.repositorios.inventario.ProductoRepositorio;
import com.inventory.repositorios.inventario.InventarioRepositorio;
import com.inventory.repositorios.inventario.MovimientoInventarioRepositorio;
import com.inventory.modelo.entidades.inventario.Producto;
import com.inventory.repositorios.proveedores.ProductoProveedorRepositorio;
import com.inventory.modelo.entidades.proveedores.ProductoProveedor;
import com.inventory.modelo.entidades.inventario.Inventario;
import com.inventory.modelo.entidades.inventario.MovimientoInventario;
import com.inventory.modelo.entidades.inventario.TipoMovimiento;
import com.inventory.repositorios.nucleo.SucursalRepositorio;
import com.inventory.modelo.entidades.nucleo.Sucursal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServicioImpl implements InventarioServicio {
    private final ProductoRepositorio productRepository;
    private final InventarioRepositorio inventoryRepository;
    private final MovimientoInventarioRepositorio movementRepository;
    private final PublicadorEventos eventPublisher;
    private final ProductoProveedorRepositorio productProviderRepository;
    private final SucursalRepositorio sucursalRepository;

    @Override
    @Transactional
    public ProductoDetalleDTO createProduct(ProductoCrearDTO dto) {
        java.math.BigDecimal costBase = java.math.BigDecimal.ZERO;
        if (dto.detalleProdcutoCrearDTO() != null && !dto.detalleProdcutoCrearDTO().isEmpty()) {
            List<BigDecimal> prices = dto.detalleProdcutoCrearDTO().stream()
                    .map(DetalleProdcutoCrearDTO::precioCostoPromedio)
                    .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

            if (!prices.isEmpty()) {
                BigDecimal total = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                costBase = total.divide(BigDecimal.valueOf(prices.size()), 2, java.math.RoundingMode.HALF_UP);
            }
        }

        Producto product = Producto.builder()
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .sku(dto.sku())
                .unidadMedidaBase(dto.unidadMedidaBase())
                .precioCostoPromedio(costBase)
                .activo(dto.activo())
                .build();
        Producto saved = productRepository.save(product);

        if (dto.idProveedor() != null) {
            log.info("Relacionando producto con proveedor ID: {}", dto.idProveedor());
            ProductoProveedor pp = ProductoProveedor.builder()
                    .productoId(saved.getId())
                    .proveedorId(dto.idProveedor())
                    .precioCompra(saved.getPrecioCostoPromedio())
                    .leadTimeDias(0)
                    .build();
            productProviderRepository.save(pp);
        } else {
            log.warn("No se proporcionó idProveedor para el producto: {}", saved.getNombre());
        }

        // Inicializar inventario en todas las sucursales
        List<Sucursal> todasLasSucursales = sucursalRepository.findAll();
        Map<Long, DetalleProdcutoCrearDTO> detallesMap = dto.detalleProdcutoCrearDTO() != null
                ? dto.detalleProdcutoCrearDTO().stream()
                        .filter(d -> d.idSucursal() != null)
                        .collect(Collectors.toMap(DetalleProdcutoCrearDTO::idSucursal, d -> d))
                : Collections.emptyMap();

        for (Sucursal sucursal : todasLasSucursales) {
            DetalleProdcutoCrearDTO detail = detallesMap.get(sucursal.getId());

            BigDecimal stock = BigDecimal.ZERO;
            BigDecimal stockMinimo = BigDecimal.ZERO;
            BigDecimal precioSucursal = costBase;
            boolean activo = false;

            if (detail != null) {
                stock = detail.cantidadInicial() == null ? BigDecimal.ZERO : detail.cantidadInicial();
                stockMinimo = detail.cantidadMinima() == null ? BigDecimal.ZERO : detail.cantidadMinima();
                activo = dto.activo();
                if (detail.precioCostoPromedio() != null) {
                    precioSucursal = detail.precioCostoPromedio();
                }
            }

            Inventario inventario = Inventario.builder()
                    .producto(saved)
                    .sucursal(sucursal)
                    .stock(stock)
                    .stockMinimo(stockMinimo)
                    .activo(activo)
                    .precioCostoPromedio(precioSucursal)
                    .build();
            inventoryRepository.save(inventario);

            if (detail != null && stock.compareTo(BigDecimal.ZERO) > 0) {
                MovimientoInventario movement = MovimientoInventario.builder()
                        .tipo(TipoMovimiento.ENTRADA_COMPRA)
                        .cantidad(stock)
                        .fechaMovimiento(LocalDateTime.now())
                        .sucursalId(sucursal.getId())
                        .productoId(saved.getId())
                        .motivo("Inventario inicial")
                        .build();
                movementRepository.save(movement);
            }
        }

        eventPublisher.publicarAuditoria("1", "CREATE", "Producto", saved.getId(), "Created product");
        return toDetalleDTO(saved);
    }

    @Override
    @Transactional
    public ProductoDetalleDTO updateProduct(Long id, ProductoEditarDTO dto) {
        log.info(
                ">>> [updateProduct] id={} | nombre={} | descripcion={} | sku={} | unidadMedida={} | precioCosto={} | stock={} | activo={} | idSucursal={} | idProveedor={} | idUsuario={} | razonCambio={}",
                id, dto.nombre(), dto.descripcion(), dto.sku(), dto.unidadMedidaBase(),
                dto.precioCostoPromedio(), dto.stock(), dto.activo(),
                dto.idSucursal(), dto.idProveedor(), dto.idUsuarioResponsable(), dto.razonCambio());
        Producto product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setNombre(dto.nombre());
        product.setDescripcion(dto.descripcion());
        product.setSku(dto.sku());
        product.setUnidadMedidaBase(dto.unidadMedidaBase());
        product.setPrecioCostoPromedio(
                dto.precioCostoPromedio() == null ? java.math.BigDecimal.ZERO : dto.precioCostoPromedio());

        Producto saved = productRepository.save(product);

        // Actualización de stock por sucursal
        if (dto.idSucursal() != null && dto.stock() != null) {
            Inventario inv = inventoryRepository.findByProducto_IdAndSucursal_Id(product.getId(), dto.idSucursal())
                    .orElseGet(() -> {
                        com.inventory.modelo.entidades.nucleo.Sucursal sucursal = com.inventory.modelo.entidades.nucleo.Sucursal
                                .builder()
                                .id(dto.idSucursal()).build();
                        return Inventario.builder()
                                .producto(product)
                                .sucursal(sucursal)
                                .stock(java.math.BigDecimal.ZERO)
                                .stockMinimo(java.math.BigDecimal.ZERO)
                                .activo(dto.activo())
                                .precioCostoPromedio(dto.precioCostoPromedio() == null ? java.math.BigDecimal.ZERO
                                        : dto.precioCostoPromedio())
                                .build();
                    });
            inv.setActivo(dto.activo());
            inv.setStock(dto.stock());
            inventoryRepository.save(inv);

            MovimientoInventario movement = MovimientoInventario.builder()
                    .tipo(TipoMovimiento.AJUSTE)
                    .cantidad(dto.stock())
                    .fechaMovimiento(LocalDateTime.now())
                    .sucursalId(dto.idSucursal())
                    .productoId(product.getId())
                    .usuarioId(dto.idUsuarioResponsable())
                    .motivo(dto.razonCambio() != null ? dto.razonCambio() : "Actualización desde edición de producto")
                    .build();
            movementRepository.save(movement);

        }

        // Actualización de proveedor
        if (dto.idProveedor() != null) {
            ProductoProveedor pp = productProviderRepository.findByProductoId(product.getId(), PageRequest.of(0, 1))
                    .getContent().stream().findFirst().orElse(null);

            if (pp != null) {
                if (!pp.getProveedorId().equals(dto.idProveedor())) {
                    pp.setProveedorId(dto.idProveedor());
                    productProviderRepository.save(pp);
                }
            } else {
                pp = ProductoProveedor.builder()
                        .productoId(product.getId())
                        .proveedorId(dto.idProveedor())
                        .precioCompra(product.getPrecioCostoPromedio())
                        .leadTimeDias(0)
                        .build();
                productProviderRepository.save(pp);
            }
        }

        String usuarioId = dto.idUsuarioResponsable() != null ? dto.idUsuarioResponsable().toString() : "1";
        eventPublisher.publicarAuditoria(usuarioId, "UPDATE", "Producto", saved.getId(),
                "Producto actualizado: " + (dto.razonCambio() != null ? dto.razonCambio() : "Sin motivo especificado"));

        return toDetalleDTO(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long idProducto, Long idSucursal) {
        if (idSucursal != null) {
            inventoryRepository.updateActivoStatus(idProducto, idSucursal, false);
            eventPublisher.publicarAuditoria("1", "DELETE_BRANCH_INVENTORY", "Inventario", idProducto,
                    "Producto desactivado en sucursal ID: " + idSucursal);
        } else {
            Producto product = productRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            product.setActivo(false);
            productRepository.save(product);
            eventPublisher.publicarAuditoria("1", "DELETE_PRODUCT", "Producto", idProducto,
                    "Producto desactivado globalmente");
        }
    }

    @Override
    public Page<ProductoInformacionDTO> getProducts(Integer pagina, Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
        return productRepository.findAll(pageable).map(this::toInformacionDTO);
    }

    @Override
    public ProductoDetallePorSucursalDTO getProductByIdSucursal(Long idSucursal, Long idProducto) {
        return inventoryRepository.findProductDetailByBranchAndProduct(idSucursal, idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en esta sucursal"));
    }

    @Override
    public Page<InventarioRespuestaDTO> getInventoryByBranch(Long branchId, Boolean activo, Integer pagina,
            Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
        return inventoryRepository.findCatalogByBranch(branchId, activo, pageable).map(this::enrichInventarioRespuesta);
    }

    private InventarioRespuestaDTO enrichInventarioRespuesta(InventarioRespuestaDTO dto) {
        Long providerId = productProviderRepository.findByProductoId(dto.idProducto(), PageRequest.of(0, 1))
                .getContent().stream().findFirst().map(ProductoProveedor::getProveedorId).orElse(null);

        return new InventarioRespuestaDTO(
                dto.idProducto(), dto.nombreProducto(), dto.sku(), dto.unidadMedida(),
                dto.descripcion(), dto.activo(), dto.idSucursal(), dto.stock(),
                dto.stockMinimo(), dto.precioCostoPromedio(), providerId);
    }

    @Override
    @Transactional
    public void updateStock(Long productId, Long branchId, Double quantity, String type, String reason,
            String usuarioResponsable) {
        Inventario inv = inventoryRepository.findByProducto_IdAndSucursal_Id(productId, branchId)
                .orElseThrow(() -> new RuntimeException("Inventario not found"));

        java.math.BigDecimal amount = java.math.BigDecimal.valueOf(quantity);

        if (type.equals("OUT")) {
            if (inv.getStock().compareTo(amount) < 0) {
                throw new RuntimeException("Stock insuficiente para realizar esta operación.");
            }
            inv.setStock(inv.getStock().subtract(amount));
        } else if (type.equals("IN")) {
            inv.setStock(inv.getStock().add(amount));
        } else {
            throw new RuntimeException("Tipo de operación no válido");
        }
        inventoryRepository.save(inv);

        MovimientoInventario movement = MovimientoInventario.builder()
                .tipo(type.equals("IN") ? TipoMovimiento.ENTRADA_COMPRA : TipoMovimiento.SALIDA_VENTA)
                .cantidad(amount)
                .fechaMovimiento(LocalDateTime.now())
                .sucursalId(branchId)
                .productoId(productId)
                .motivo(reason != null ? reason : "Actualización de stock")
                .build();
        movementRepository.save(movement);

        if (inv.getStock().compareTo(inv.getStockMinimo()) < 0) {
            eventPublisher.publicarActualizacionStock(inv, usuarioResponsable);
        }

        eventPublisher.publicarAuditoria(usuarioResponsable, "UPDATE_STOCK", "Inventario", inv.getProductoId(),
                "Stock updated: " + quantity);
    }

    @Override
    @Transactional
    public void procesarEntradaCompra(Long productId, Long branchId, BigDecimal quantity, BigDecimal unitCost,
            String reason, String usuarioResponsable) {
        log.info("Procesando entrada por compra: Producto={}, Sucursal={}, Cantidad={}, Costo={}",
                productId, branchId, quantity, unitCost);

        Inventario inv = inventoryRepository.findByProducto_IdAndSucursal_Id(productId, branchId)
                .orElseGet(() -> {
                    Producto p = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                    Sucursal s = sucursalRepository.findById(branchId)
                            .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
                    return Inventario.builder()
                            .producto(p)
                            .sucursal(s)
                            .stock(BigDecimal.ZERO)
                            .precioCostoPromedio(BigDecimal.ZERO)
                            .activo(true)
                            .build();
                });

        BigDecimal stockActual = inv.getStock();
        BigDecimal cppActual = inv.getPrecioCostoPromedio();

        // Fórmula: CPP_nuevo = (Stock_actual * CPP_actual + Cantidad_recibida * Precio_compra) / (Stock_actual + Cantidad_recibida)
        BigDecimal valorActualTotal = stockActual.multiply(cppActual);
        BigDecimal valorNuevaCompra = quantity.multiply(unitCost);
        BigDecimal nuevoStock = stockActual.add(quantity);

        BigDecimal nuevoCPP;
        if (nuevoStock.compareTo(BigDecimal.ZERO) > 0) {
            nuevoCPP = valorActualTotal.add(valorNuevaCompra)
                    .divide(nuevoStock, 4, java.math.RoundingMode.HALF_UP);
        } else {
            nuevoCPP = unitCost;
        }

        inv.setStock(nuevoStock);
        inv.setPrecioCostoPromedio(nuevoCPP);
        inventoryRepository.save(inv);

        // Actualizar el costo en la entidad Producto (último costo de compra)
        Producto product = inv.getProducto();
        product.setPrecioCostoPromedio(unitCost);
        productRepository.save(product);

        // Registrar movimiento
        MovimientoInventario movement = MovimientoInventario.builder()
                .tipo(TipoMovimiento.ENTRADA_COMPRA)
                .cantidad(quantity)
                .fechaMovimiento(LocalDateTime.now())
                .sucursalId(branchId)
                .productoId(productId)
                .motivo(reason != null ? reason : "Entrada por compra")
                .build();
        movementRepository.save(movement);

        eventPublisher.publicarAuditoria(usuarioResponsable, "ENTRADA_COMPRA", "Inventario", productId,
                "Entrada por compra registrada. Nuevo stock: " + nuevoStock + ", Nuevo CPP: " + nuevoCPP);
    }

    @Override
    public Page<InventarioInformacionDTO> getLowStockProducts(Integer pagina, Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
        return inventoryRepository.findByQuantityLessThanMinStock(pageable).map(this::toInventarioInformacion);
    }

    @Override
    public Page<InventarioRespuestaDTO> getCatalogoActivo(Long branchId, Boolean activo, Integer pagina,
            Integer porPagina) {
        int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
        return inventoryRepository.findCatalogByBranch(branchId, activo, pageable);
    }

    private ProductoInformacionDTO toInformacionDTO(Producto product) {
        java.math.BigDecimal stockTotal = inventoryRepository.sumStockByProductoId(product.getId());
        Long providerId = productProviderRepository.findByProductoId(product.getId(), PageRequest.of(0, 1))
                .getContent().stream().findFirst().map(ProductoProveedor::getProveedorId).orElse(null);

        return new ProductoInformacionDTO(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getSku(),
                product.getUnidadMedidaBase(),
                product.getPrecioCostoPromedio(),
                product.getActivo(),
                stockTotal != null ? stockTotal : java.math.BigDecimal.ZERO,
                providerId);
    }

    private ProductoDetalleDTO toDetalleDTO(Producto product) {
        return new ProductoDetalleDTO(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getSku(),
                product.getUnidadMedidaBase(),
                product.getPrecioCostoPromedio());
    }

    private InventarioInformacionDTO toInventarioInformacion(Inventario inventory) {
        return new InventarioInformacionDTO(
                inventory.getSucursal().getId(),
                inventory.getProducto().getId(),
                inventory.getProducto().getNombre(),
                inventory.getProducto().getSku(),
                inventory.getProducto().getDescripcion(),
                inventory.getStock(),
                inventory.getStockMinimo());
    }
}
