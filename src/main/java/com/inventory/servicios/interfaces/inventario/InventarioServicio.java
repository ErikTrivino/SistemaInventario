    package com.inventory.servicios.interfaces.inventario;
    import com.inventory.modelo.dto.inventario.InventarioInformacionDTO;
    import com.inventory.modelo.dto.inventario.ProductoCrearDTO;
    import com.inventory.modelo.dto.inventario.ProductoDetalleDTO;
    import com.inventory.modelo.dto.inventario.ProductoEditarDTO;
    import com.inventory.modelo.dto.inventario.ProductoInformacionDTO;
    import org.springframework.data.domain.Page;

    public interface InventarioServicio {
        ProductoDetalleDTO createProduct(ProductoCrearDTO dto);
        ProductoDetalleDTO updateProduct(Long id, ProductoEditarDTO dto);
        void deleteProduct(Long id);
        Page<ProductoInformacionDTO> getProducts(Integer pagina, Integer porPagina);
        Page<InventarioInformacionDTO> getInventoryByBranch(Long branchId, Integer pagina, Integer porPagina);
        void updateStock(Long productId, Long branchId, Double quantity, String type, String reason, String usuarioResponsable);
        Page<InventarioInformacionDTO> getLowStockProducts(Integer pagina, Integer porPagina);
        Page<com.inventory.modelo.dto.inventario.InventarioRespuestaDTO> getCatalogoActivo(Long branchId, Integer pagina, Integer porPagina);
    }



