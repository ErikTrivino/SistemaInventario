    package com.inventory.servicios.implementaciones.logistica;
    import com.inventory.servicios.interfaces.logistica.LogisticaServicio;
    import com.inventory.modelo.entidades.logistica.Envio;
    import com.inventory.repositorios.logistica.EnvioRepositorio;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;

    @Service
    @RequiredArgsConstructor
    public class LogisticaServicioImpl implements LogisticaServicio {
        private final EnvioRepositorio envioRepositorio;

        @Override public Envio createShipment(Long transferId) { return null; }
        @Override public void updateShipmentStatus(Long shipmentId, String status) { }
        @Override public void compareDeliveryTimes() { }

        @Override
        public Page<Envio> getShipments(Integer pagina, Integer porPagina) {
            int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
            int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
            Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
            return envioRepositorio.findAll(pageable);
        }
    }



