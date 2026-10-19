package com.littlehorizons.erp.service.invoice;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;

/**
 * Simulated CUFE (Código Único de Factura Electrónica).
 * <p>DIAN builds the CUFE with SHA-384 over the invoice data. Here we hash
 * invoice number + issuer NIT + issue date + total, as the requirement asks.</p>
 */
public final class CufeGenerator {

    private CufeGenerator() {
    }

    /** @return 96 hexadecimal characters (SHA-384) */
    public static String generate(String invoiceNumber, String nit, LocalDate issueDate, BigDecimal total) {
        String source = invoiceNumber + nit + issueDate + total.toPlainString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-384 is mandatory in every Java runtime, so this cannot happen
            throw new IllegalStateException("SHA-384 not available", e);
        }
    }
}
