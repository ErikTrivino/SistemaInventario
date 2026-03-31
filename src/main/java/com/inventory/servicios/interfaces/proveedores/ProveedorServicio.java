package com.inventory.servicios.interfaces.proveedores;

import com.inventory.modelo.dto.proveedores.*;
import org.springframework.data.domain.Page;

public interface ProveedorServicio {
    /** RF-38 */
    ProveedorInformacionDTO createSupplier(ProveedorCrearDTO dto);
    ProveedorInformacionDTO updateSupplier(Long id, ProveedorEditarDTO dto);
    ProveedorInformacionDTO toggleActivo(Long id);
    Page<ProveedorInformacionDTO> getSuppliers(Integer pagina, Integer porPagina);
    Page<ProveedorInformacionDTO> getAllSuppliers(Integer pagina, Integer porPagina);

    /** RF-39 */
    ProductoProveedorInformacionDTO registrarListaPrecio(ProductoProveedorDTO dto);
    Page<ProductoProveedorInformacionDTO> getListaPreciosPorProveedor(Long supplierId, Integer pagina, Integer porPagina);
    Page<ProductoProveedorInformacionDTO> getListaPreciosPorProducto(Long productId, Integer pagina, Integer porPagina);

    /** RF-41/RF-42 */
    CumplimientoProveedorDTO calcularCumplimiento(Long supplierId);

    /** Legacy */
    void assignProductToSupplier(Long supplierId, Long productId);
}



