    package com.inventory.servicios.implementaciones.reportes;
    import com.inventory.servicios.interfaces.reportes.ReporteServicio;
    import org.springframework.stereotype.Service;
    import lombok.RequiredArgsConstructor;
    import java.util.Date;

    @Service
    @RequiredArgsConstructor
    public class ReporteServicioImpl implements ReporteServicio {
        @Override public Object generateInventoryReport(Date start, Date end) { return null; }
        @Override public Object generateSalesReport(Date start, Date end) { return null; }
        @Override public Object generateTransferReport(Date start, Date end) { return null; }
    }



