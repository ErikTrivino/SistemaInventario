    package com.inventory.servicios.interfaces.reportes;
    import java.util.Date;

    public interface ReporteServicio {
        Object generateInventoryReport(Date start, Date end);
        Object generateSalesReport(Date start, Date end);
        Object generateTransferReport(Date start, Date end);
    }



