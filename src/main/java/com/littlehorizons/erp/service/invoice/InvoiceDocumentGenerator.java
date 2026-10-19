package com.littlehorizons.erp.service.invoice;

/**
 * Produces the printable invoice file. The payment service depends on this interface,
 * not on OpenPDF (Dependency Inversion): the library could be replaced by PDFBox
 * without touching the business rules.
 */
public interface InvoiceDocumentGenerator {

    /**
     * Writes the invoice file.
     *
     * @return absolute path of the generated file
     */
    String generate(InvoiceDocument document);
}
