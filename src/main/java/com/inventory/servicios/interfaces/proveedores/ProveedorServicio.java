    package com.inventory.servicios.interfaces.proveedores;
    import com.inventory.modelo.dto.proveedores.ProveedorCrearDTO;
    import com.inventory.modelo.dto.proveedores.ProveedorEditarDTO;
    import com.inventory.modelo.dto.proveedores.ProveedorInformacionDTO;
    import java.util.List;

    public interface ProveedorServicio {
        ProveedorInformacionDTO createSupplier(ProveedorCrearDTO dto);
        ProveedorInformacionDTO updateSupplier(Long id, ProveedorEditarDTO dto);
        List<ProveedorInformacionDTO> getSuppliers();
        void assignProductToSupplier(Long supplierId, Long productId);
    }



