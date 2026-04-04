    package com.inventory.servicios.implementaciones.logistica;
    import com.inventory.servicios.interfaces.logistica.LogisticaServicio;
    import com.inventory.modelo.entidades.logistica.Envio;
    import com.inventory.repositorios.logistica.EnvioRepositorio;
    import com.inventory.modelo.dto.logistica.EnvioSeguimientoDTO;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;
    import java.time.temporal.ChronoUnit;
    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    public class LogisticaServicioImpl implements LogisticaServicio {
        private final EnvioRepositorio envioRepositorio;

        @Override public Envio createShipment(Long transferId) { return null; }
        @Override public void updateShipmentStatus(Long shipmentId, String status) { }
        
        @Override
        public List<EnvioSeguimientoDTO> compareDeliveryTimes() {
            return envioRepositorio.findByFechaRecepcionRealIsNotNull().stream()
                .map(envio -> {
                    long tiempoReal = 0;
                    if (envio.getFechaDespacho() != null && envio.getFechaRecepcionReal() != null) {
                        tiempoReal = ChronoUnit.DAYS.between(
                            envio.getFechaDespacho().toLocalDate(), 
                            envio.getFechaRecepcionReal().toLocalDate()
                        );
                    }
                    
                    int tiempoEstimado = envio.getTiempoEstimadoEntrega() != null ? envio.getTiempoEstimadoEntrega() : 0;
                    long desviacion = tiempoReal - tiempoEstimado;
                    
                    return new EnvioSeguimientoDTO(
                        envio.getId(),
                        envio.getTransferencia() != null ? envio.getTransferencia().getId() : null,
                        tiempoEstimado,
                        tiempoReal,
                        desviacion
                    );
                })
                .collect(Collectors.toList());
        }

        @Override
        public Page<Envio> getShipments(Integer pagina, Integer porPagina) {
            int numPagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
            int tamanoPagina = (porPagina != null && porPagina > 0) ? porPagina : 10;
            Pageable pageable = PageRequest.of(numPagina, tamanoPagina);
            return envioRepositorio.findAll(pageable);
        }
    }



