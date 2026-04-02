    package com.inventory.servicios.interfaces.inventario;
    import com.inventory.modelo.dto.inventario.*;
    import org.springframework.data.domain.Page;

    public interface InventarioServicio {
        ProductoDetalleDTO createProduct(ProductoCrearDTO dto);
        ProductoDetalleDTO updateProduct(Long id, ProductoEditarDTO dto);
        void deleteProduct(Long idProducto, Long idSucursal);
        Page<ProductoInformacionDTO> getProducts(Integer pagina, Integer porPagina);
        ProductoDetallePorSucursalDTO getProductByIdSucursal(Long idSucursal, Long idProducto);
        Page<com.inventory.modelo.dto.inventario.InventarioRespuestaDTO> getInventoryByBranch(Long branchId, Boolean activo, Integer pagina, Integer porPagina);
        void updateStock(Long productId, Long branchId, Double quantity, String type, String reason, String usuarioResponsable);
        Page<InventarioInformacionDTO> getLowStockProducts(Integer pagina, Integer porPagina);
        Page<com.inventory.modelo.dto.inventario.InventarioRespuestaDTO> getCatalogoActivo(Long branchId, Boolean activo, Integer pagina, Integer porPagina);
    }



