    package com.inventory.servicios.interfaces.ventas;
    import com.inventory.modelo.dto.ventas.VentaCrearDTO;
    import com.inventory.modelo.dto.ventas.VentaInformacionDTO;
    import com.inventory.modelo.dto.ventas.ValidacionStockDTO;
    import java.util.Date;
    import java.math.BigDecimal;
    import org.springframework.data.domain.Page;

    public interface VentaServicio {
        ValidacionStockDTO validateStock(Long productId, Long branchId, BigDecimal quantity);
        VentaInformacionDTO createSale(VentaCrearDTO dto, Long userId);
        Page<VentaInformacionDTO> getSalesByBranch(Long branchId, Integer pagina, Integer porPagina);
        Page<VentaInformacionDTO> getSalesByDateRange(Date start, Date end, Integer pagina, Integer porPagina);
    }



