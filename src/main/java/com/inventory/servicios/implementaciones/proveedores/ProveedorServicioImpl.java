    package com.inventory.servicios.implementaciones.proveedores;
    import com.inventory.servicios.interfaces.proveedores.ProveedorServicio;
    import com.inventory.modelo.dto.proveedores.ProveedorCrearDTO;
    import com.inventory.modelo.dto.proveedores.ProveedorEditarDTO;
    import com.inventory.modelo.dto.proveedores.ProveedorInformacionDTO;
    import com.inventory.modelo.entidades.proveedores.Proveedor;
    import com.inventory.repositorios.proveedores.ProveedorRepositorio;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class ProveedorServicioImpl implements ProveedorServicio {
        private final ProveedorRepositorio supplierRepository;

        @Override
        public ProveedorInformacionDTO createSupplier(ProveedorCrearDTO dto) {
            Proveedor supplier = Proveedor.builder()
                    .taxId(dto.nitRut())
                    .name(dto.razonSocial())
                    .contact(dto.contacto())
                    .build();
            return toInfo(supplierRepository.save(supplier));
        }

        @Override
        public ProveedorInformacionDTO updateSupplier(Long id, ProveedorEditarDTO dto) {
            Proveedor supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            supplier.setTaxId(dto.nitRut());
            supplier.setName(dto.razonSocial());
            supplier.setContact(dto.contacto());
            return toInfo(supplierRepository.save(supplier));
        }

        @Override
        public List<ProveedorInformacionDTO> getSuppliers() {
            return supplierRepository.findAll().stream().map(this::toInfo).toList();
        }

        @Override public void assignProductToSupplier(Long supplierId, Long productId) { }

        private ProveedorInformacionDTO toInfo(Proveedor supplier) {
            return new ProveedorInformacionDTO(
                    supplier.getId(),
                    supplier.getTaxId(),
                    supplier.getName(),
                    supplier.getContact()
            );
        }
    }



