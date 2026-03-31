package com.inventory.servicios.implementaciones.proveedores;

import com.inventory.servicios.interfaces.proveedores.ProveedorServicio;
import com.inventory.modelo.dto.proveedores.*;
import com.inventory.modelo.entidades.proveedores.Proveedor;
import com.inventory.modelo.entidades.proveedores.ProductoProveedor;
import com.inventory.repositorios.proveedores.ProveedorRepositorio;
import com.inventory.repositorios.proveedores.ProductoProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@Service
@RequiredArgsConstructor
public class ProveedorServicioImpl implements ProveedorServicio {
    private final ProveedorRepositorio supplierRepository;
    private final ProductoProveedorRepositorio supplierProductRepository;

    /** RF-38: Crear proveedor con NIT único y estado activo por defecto. */
    @Override
    @Transactional
    public ProveedorInformacionDTO createSupplier(ProveedorCrearDTO dto) {
        if (supplierRepository.findByNitRut(dto.nitRut()).isPresent()) {
            throw new RuntimeException("Ya existe un proveedor con el NIT/RUT: " + dto.nitRut());
        }
        Proveedor supplier = Proveedor.builder()
                .nitRut(dto.nitRut())
                .razonSocial(dto.razonSocial())
                .contacto(dto.contacto())
                .email(dto.email())
                .activo(true)
                .build();
        return toInfo(supplierRepository.save(supplier));
    }

    /** RF-38: Actualizar datos del proveedor (incluyendo activar/desactivar). */
    @Override
    @Transactional
    public ProveedorInformacionDTO updateSupplier(Long id, ProveedorEditarDTO dto) {
        Proveedor supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
        supplier.setRazonSocial(dto.razonSocial());
        supplier.setContacto(dto.contacto());
        return toInfo(supplierRepository.save(supplier));
    }

    /** RF-38: Activar o desactivar lógicamente un proveedor. */
    @Override
    @Transactional
    public ProveedorInformacionDTO toggleActivo(Long id) {
        Proveedor supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
        supplier.setActivo(!supplier.isActivo());
        return toInfo(supplierRepository.save(supplier));
    }

    /** RF-38: Listar solo proveedores activos. */
    @Override
    public Page<ProveedorInformacionDTO> getSuppliers(Integer pagina, Integer porPagina) {
        int page = (pagina != null) ? pagina : 0;
        int size = (porPagina != null && porPagina > 0) ? porPagina : 10;
        return supplierRepository.findByActivoTrue(PageRequest.of(page, size)).map(this::toInfo);
    }

    /** RF-38: Listar todos los proveedores (activos e inactivos). */
    @Override
    public Page<ProveedorInformacionDTO> getAllSuppliers(Integer pagina, Integer porPagina) {
        int page = (pagina != null) ? pagina : 0;
        int size = (porPagina != null && porPagina > 0) ? porPagina : 10;
        return supplierRepository.findAll(PageRequest.of(page, size)).map(this::toInfo);
    }

    /** RF-39: Registrar condiciones de precio y lead-time para un producto-proveedor. */
    @Override
    @Transactional
    public ProductoProveedorInformacionDTO registrarListaPrecio(ProductoProveedorDTO dto) {
        if (dto.precioCompra().doubleValue() <= 0) {
            throw new RuntimeException("El precio de compra debe ser mayor a cero.");
        }
        if (dto.descuentoProveedor() != null &&
            (dto.descuentoProveedor().doubleValue() < 0 || dto.descuentoProveedor().doubleValue() > 100)) {
            throw new RuntimeException("El descuento del proveedor debe estar entre 0 y 100.");
        }
        if (dto.leadTimeDias() < 0) {
            throw new RuntimeException("El lead time debe ser mayor o igual a cero.");
        }

        ProductoProveedor pp = ProductoProveedor.builder()
                .proveedorId(dto.idProveedor())
                .productoId(dto.idProducto())
                .precioCompra(dto.precioCompra())
                .descuentoProveedor(dto.descuentoProveedor() != null ? dto.descuentoProveedor() : java.math.BigDecimal.ZERO)
                .leadTimeDias(dto.leadTimeDias())
                .fechaVigenciaDesde(dto.fechaVigenciaDesde() != null ? dto.fechaVigenciaDesde() : java.time.LocalDate.now())
                .build();

        return toPPInfo(supplierProductRepository.save(pp));
    }

    /** RF-39 / RF-43: Historial de precios pactados con un proveedor (fluctuación de precios). */
    @Override
    public Page<ProductoProveedorInformacionDTO> getListaPreciosPorProveedor(Long supplierId, Integer pagina, Integer porPagina) {
        int page = (pagina != null) ? pagina : 0;
        int size = (porPagina != null && porPagina > 0) ? porPagina : 10;
        return supplierProductRepository.findByProveedorId(supplierId, PageRequest.of(page, size))
                .map(this::toPPInfo);
    }

    /** RF-39: Lista de precios disponibles para un producto (todos sus proveedores). */
    @Override
    public Page<ProductoProveedorInformacionDTO> getListaPreciosPorProducto(Long productId, Integer pagina, Integer porPagina) {
        int page = (pagina != null) ? pagina : 0;
        int size = (porPagina != null && porPagina > 0) ? porPagina : 10;
        return supplierProductRepository.findByProductoId(productId, PageRequest.of(page, size))
                .map(this::toPPInfo);
    }

    /** RF-41/RF-42: Calcular KPI de cumplimiento (recepciones a tiempo vs. total). */
    @Override
    public CumplimientoProveedorDTO calcularCumplimiento(Long supplierId) {
        return supplierProductRepository.calcularCumplimientoProveedor(supplierId);
    }

    @Override
    public void assignProductToSupplier(Long supplierId, Long productId) {
        // Método legacy - redirige a registrarListaPrecio con valores por defecto
        throw new RuntimeException("Use POST /api/proveedores/{id}/lista-precios para registrar condiciones.");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private ProveedorInformacionDTO toInfo(Proveedor s) {
        return new ProveedorInformacionDTO(
                s.getId(), s.getNitRut(), s.getRazonSocial(), s.getContacto(), s.getEmail(), s.isActivo());
    }

    private ProductoProveedorInformacionDTO toPPInfo(ProductoProveedor pp) {
        return new ProductoProveedorInformacionDTO(
                pp.getId(), pp.getProveedorId(), pp.getProductoId(),
                pp.getPrecioCompra(), pp.getDescuentoProveedor(),
                pp.getLeadTimeDias(), pp.getFechaVigenciaDesde());
    }
}



