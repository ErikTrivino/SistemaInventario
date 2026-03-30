    package com.inventory.servicios.interfaces.ventas;
    import com.inventory.modelo.dto.ventas.VentaCrearDTO;
    import com.inventory.modelo.dto.ventas.VentaInformacionDTO;
    import java.util.Date;
    import java.util.List;

    public interface VentaServicio {
        VentaInformacionDTO createSale(VentaCrearDTO dto);
        List<VentaInformacionDTO> getSalesByBranch(Long branchId);
        List<VentaInformacionDTO> getSalesByDateRange(Date start, Date end);
    }



