    package com.inventory.servicios.interfaces.compras;
    import com.inventory.modelo.dto.compras.CompraCrearDTO;
    import com.inventory.modelo.dto.compras.CompraInformacionDTO;
    import java.util.List;

    public interface CompraServicio {
        CompraInformacionDTO createPurchase(CompraCrearDTO dto);
        void receivePurchase(Long purchaseId);
        List<CompraInformacionDTO> getPurchasesBySupplier(Long supplierId);
    }



