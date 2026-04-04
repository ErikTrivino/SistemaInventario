    package com.inventory.servicios.interfaces.logistica;
    import com.inventory.modelo.entidades.logistica.Envio;
    import com.inventory.modelo.dto.logistica.EnvioSeguimientoDTO;
    import org.springframework.data.domain.Page;
    import java.util.List;

    public interface LogisticaServicio {
        Envio createShipment(Long transferId);
        void updateShipmentStatus(Long shipmentId, String status);
        List<EnvioSeguimientoDTO> compareDeliveryTimes();
        Page<Envio> getShipments(Integer pagina, Integer porPagina);
    }



