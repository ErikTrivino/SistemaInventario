    package com.inventory.servicios.interfaces.compras;
    import com.inventory.modelo.dto.compras.OrdenCompraCrearDTO;
    import com.inventory.modelo.dto.compras.OrdenCompraRecepcionDTO;
    import com.inventory.modelo.dto.compras.CompraHistoricoRespuestaDTO;
    import com.inventory.modelo.dto.compras.CompraInformacionDTO;
    import java.time.LocalDateTime;
    import org.springframework.data.domain.Page;

    public interface CompraServicio {
        CompraInformacionDTO crearCompra(OrdenCompraCrearDTO dto, Long userId);
        void recibirCompra(OrdenCompraRecepcionDTO dto);
        Page<CompraHistoricoRespuestaDTO> obtenerHistoricoCompras(Long idProveedor, Long idProducto,Long idSucursal, LocalDateTime fechaDesde, LocalDateTime fechaHasta, Integer pagina, Integer porPagina);
    }



