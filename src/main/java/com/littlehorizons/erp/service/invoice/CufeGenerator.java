package com.littlehorizons.erp.service.invoice;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;

/**
 * CUFE simulado (Código Único de Factura Electrónica).
 * <p>La DIAN construye el CUFE con SHA-384 sobre los datos de la factura. Aquí se calcula el hash de
 * número de factura + NIT del emisor + fecha + total, como pide el enunciado.</p>
 */
public final class CufeGenerator {

    // Constructor privado: clase de utilidad con un método estático
    private CufeGenerator() {
    }

    /** @return 96 caracteres hexadecimales (el hash SHA-384) */
    public static String generate(String invoiceNumber, String nit, LocalDate issueDate, BigDecimal total) {
        // Se juntan los datos en un solo texto: ese texto es lo que se convierte en hash
        String source = invoiceNumber + nit + issueDate + total.toPlainString();
        try {
            // SHA-384 es una función hash: el mismo texto siempre da el mismo resultado y no se puede revertir
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);          // los bytes se escriben como texto hexadecimal
        } catch (NoSuchAlgorithmException e) {
            // SHA-384 existe en todo Java, así que esto no debería ocurrir nunca
            throw new IllegalStateException("SHA-384 not available", e);
        }
    }
}
