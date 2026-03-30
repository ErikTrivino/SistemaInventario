    package com.inventory.servicios.interfaces.inventario;
    import com.inventory.modelo.dto.inventario.InventarioInformacionDTO;
    import com.inventory.modelo.dto.inventario.ProductoCrearDTO;
    import com.inventory.modelo.dto.inventario.ProductoDetalleDTO;
    import com.inventory.modelo.dto.inventario.ProductoEditarDTO;
    import com.inventory.modelo.dto.inventario.ProductoInformacionDTO;
    import java.util.List;

    public interface InventarioServicio {
        ProductoDetalleDTO createProduct(ProductoCrearDTO dto);
        ProductoDetalleDTO updateProduct(Long id, ProductoEditarDTO dto);
        void deleteProduct(Long id);
        List<ProductoInformacionDTO> getProducts();
        List<InventarioInformacionDTO> getInventoryByBranch(Long branchId);
        void updateStock(Long productId, Long branchId, Double quantity, String type, String reason);
        List<InventarioInformacionDTO> getLowStockProducts();
    }



