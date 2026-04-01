    package com.inventory.servicios.interfaces.logistica;
    import com.inventory.modelo.entidades.logistica.Envio;
    import org.springframework.data.domain.Page;

    public interface LogisticaServicio {
        Envio createShipment(Long transferId);
        void updateShipmentStatus(Long shipmentId, String status);
        void compareDeliveryTimes();
        Page<Envio> getShipments(Integer pagina, Integer porPagina);
    }



