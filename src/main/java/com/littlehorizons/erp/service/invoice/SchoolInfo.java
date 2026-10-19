package com.littlehorizons.erp.service.invoice;

/**
 * Legal data of the issuer printed on every invoice (simulated DIAN data).
 */
public final class SchoolInfo {

    public static final String LEGAL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String NIT = "900.456.789-1";
    /** NIT without dots or check digit, used to build the CUFE. */
    public static final String NIT_DIGITS = "900456789";
    public static final String ADDRESS = "Calle 127 # 15-40, Bogotá D.C., Colombia";
    public static final String PHONE = "(601) 745 8899";
    public static final String EMAIL = "facturacion@littlehorizons.edu.co";
    public static final String TAX_REGIME = "Responsable de IVA - Régimen ordinario";
    public static final String RESOLUTION = "Resolución DIAN de habilitación No. 18764000123456 del 2026-01-15 "
            + "- Prefijo FE - Rango FE-000001 a FE-100000 - Vigencia 24 meses";
    public static final String INVOICE_PREFIX = "FE-";

    private SchoolInfo() {
    }
}
