package com.inventory.servicios.interfaces.ventas;

public interface ComprobanteServicio {
    /**
     * Genera el contenido binario de un PDF para una venta específica.
     */
    byte[] generarPdfVenta(Long ventaId);

    /**
     * Almacena el contenido del PDF en el sistema de archivos.
     * @return El nombre del archivo guardado.
     */
    String guardarComprobante(byte[] content, String identificador);

    /**
     * Obtiene la representación en Base64 de un comprobante almacenado.
     */
    String obtenerBase64(Long ventaId);
}
