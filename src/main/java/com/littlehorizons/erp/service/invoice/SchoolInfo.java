package com.littlehorizons.erp.service.invoice;

/**
 * Datos legales del emisor que se imprimen en todas las facturas (datos simulados de la DIAN).
 */
public final class SchoolInfo {

    // Datos del colegio como emisor de la factura
    public static final String LEGAL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String NIT = "900.456.789-1";
    /** NIT sin puntos ni dígito de verificación; se usa para construir el CUFE. */
    public static final String NIT_DIGITS = "900456789";
    public static final String ADDRESS = "Calle 127 # 15-40, Bogotá D.C., Colombia";
    public static final String PHONE = "(601) 745 8899";
    public static final String EMAIL = "facturacion@littlehorizons.edu.co";
    public static final String TAX_REGIME = "Responsable de IVA - Régimen ordinario";
    // Resolución de habilitación de numeración (simulada) que respalda las facturas
    public static final String RESOLUTION = "Resolución DIAN de habilitación No. 18764000123456 del 2026-01-15 "
            + "- Prefijo FE - Rango FE-000001 a FE-100000 - Vigencia 24 meses";
    // Prefijo del número consecutivo de factura (FE-000123)
    public static final String INVOICE_PREFIX = "FE-";

    // Constructor privado: solo contiene constantes
    private SchoolInfo() {
    }
}
