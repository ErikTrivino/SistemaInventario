    package com.inventory.servicios.implementaciones.tablero;
    import com.inventory.servicios.interfaces.tablero.TableroServicio;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;

    @Service
    @RequiredArgsConstructor
    public class TableroServicioImpl implements TableroServicio {
        @Override public Object getSalesSummary() { return null; }
        @Override public Object getInventoryMetrics() { return null; }
        @Override public Object getTransferStatusSummary() { return null; }
    }



