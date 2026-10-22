package com.littlehorizons.erp.service.invoice;

/**
 * Produce el archivo imprimible de la factura. El servicio de pagos depende de esta interfaz y no de
 * OpenPDF (Inversión de Dependencias): se podría cambiar la librería por PDFBox sin tocar las reglas de negocio.
 */
public interface InvoiceDocumentGenerator {

    /**
     * Escribe el archivo de la factura.
     *
     * @return la ruta absoluta del archivo generado
     */
    String generate(InvoiceDocument document);
}
