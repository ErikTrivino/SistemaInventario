    package com.inventory.servicios.implementaciones.logistica;
    import com.inventory.servicios.interfaces.logistica.LogisticaServicio;
    import com.inventory.modelo.entidades.logistica.Envio;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;

    @Service
    @RequiredArgsConstructor
    public class LogisticaServicioImpl implements LogisticaServicio {
        @Override public Envio createShipment(Long transferId) { return null; }
        @Override public void updateShipmentStatus(Long shipmentId, String status) { }
        @Override public void compareDeliveryTimes() { }
    }



