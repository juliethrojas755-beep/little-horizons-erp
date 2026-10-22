package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Factura electrónica simulada (DIAN) generada para un pago.
 * <p>Tabla: {@code facturas}</p>
 */
public class Invoice {

    // ---------- Atributos (cada uno corresponde a una columna de la tabla facturas) ----------
    // Identificador único (llave primaria, la genera MySQL con AUTO_INCREMENT)
    private int id;
    // Número consecutivo de la factura (FE-000123)
    private String number;
    // Id del pago al que pertenece la factura
    private int paymentId;
    // Fecha en que se emitió la factura
    private LocalDate issueDate;
    // Valor antes de IVA
    private BigDecimal subtotal;
    // IVA incluido en el total
    private BigDecimal tax;
    // Valor total a pagar
    private BigDecimal total;
    // CUFE simulado: hash SHA-384 que identifica la factura
    private String cufe;
    // Nombre del archivo PDF (factura_000123.pdf)
    private String pdfFileName;
    // Ruta completa donde quedó guardado el PDF
    private String filePath;

    // ---------- Getters y setters ----------
    // Los atributos son privados (encapsulamiento): se leen con get/is y se modifican con set.

    /** Devuelve el identificador único. */
    public int getId() {
        return id;
    }

    /** Asigna el identificador único. */
    public void setId(int id) {
        this.id = id;
    }

    /** Devuelve el número de factura. */
    public String getNumber() {
        return number;
    }

    /** Asigna el número de factura. */
    public void setNumber(String number) {
        this.number = number;
    }

    /** Devuelve el id del pago. */
    public int getPaymentId() {
        return paymentId;
    }

    /** Asigna el id del pago. */
    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    /** Devuelve la fecha de emisión. */
    public LocalDate getIssueDate() {
        return issueDate;
    }

    /** Asigna la fecha de emisión. */
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    /** Devuelve el subtotal. */
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /** Asigna el subtotal. */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    /** Devuelve el IVA. */
    public BigDecimal getTax() {
        return tax;
    }

    /** Asigna el IVA. */
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    /** Devuelve el total. */
    public BigDecimal getTotal() {
        return total;
    }

    /** Asigna el total. */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    /** Devuelve el CUFE. */
    public String getCufe() {
        return cufe;
    }

    /** Asigna el CUFE. */
    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    /** Devuelve el nombre del PDF. */
    public String getPdfFileName() {
        return pdfFileName;
    }

    /** Asigna el nombre del PDF. */
    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    /** Devuelve la ruta del PDF. */
    public String getFilePath() {
        return filePath;
    }

    /** Asigna la ruta del PDF. */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
