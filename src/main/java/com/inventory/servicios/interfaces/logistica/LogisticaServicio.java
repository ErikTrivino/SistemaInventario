    package com.inventory.servicios.interfaces.logistica;
    import com.inventory.modelo.entidades.logistica.Envio;

    public interface LogisticaServicio {
        Envio createShipment(Long transferId);
        void updateShipmentStatus(Long shipmentId, String status);
        void compareDeliveryTimes();
    }



