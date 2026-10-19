package com.littlehorizons.erp.service.invoice;

import com.google.zxing.WriterException;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ErrorLogger;
import com.littlehorizons.erp.util.Formats;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Draws the simulated DIAN electronic invoice with OpenPDF and the QR code with ZXing.
 */
public class OpenPdfInvoiceGenerator implements InvoiceDocumentGenerator {

    public static final String OUTPUT_DIRECTORY = "invoices";

    private static final Color NAVY = new Color(17, 45, 78);
    private static final Color TEAL = new Color(0, 150, 136);
    private static final Color GOLD = new Color(244, 180, 0);
    private static final Color LIGHT = new Color(241, 245, 249);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);

    private static final Font BRAND = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.NORMAL, Color.WHITE);
    private static final Font BRAND_SUB = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Font.NORMAL, new Color(203, 213, 225));
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, NAVY);
    private static final Font NUMBER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.NORMAL, TEAL);
    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, MUTED);
    private static final Font VALUE = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, TEXT);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
    private static final Font TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL, Color.WHITE);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, MUTED);
    private static final Font MONO = FontFactory.getFont(FontFactory.COURIER, 7, Font.NORMAL, TEXT);

    @Override
    public String generate(InvoiceDocument data) {
        File directory = new File(OUTPUT_DIRECTORY);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("No se pudo crear la carpeta " + directory.getAbsolutePath());
        }
        File file = new File(directory, data.invoice().getPdfFileName());
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try (OutputStream output = new FileOutputStream(file)) {
            PdfWriter.getInstance(document, output);
            document.addTitle("Factura " + data.invoice().getNumber());
            document.addAuthor(SchoolInfo.LEGAL_NAME);
            document.open();
            document.add(header(data.invoice()));
            document.add(spacer(10));
            document.add(parties(data.payer(), data.student()));
            document.add(spacer(12));
            document.add(detail(data));
            document.add(spacer(8));
            document.add(totalsAndPayment(data.invoice(), data.payment()));
            document.add(spacer(16));
            document.add(footer(data));
            document.close();
            return file.getAbsolutePath();
        } catch (IOException | DocumentException | WriterException e) {
            ErrorLogger.getInstance().error("OpenPdfInvoiceGenerator", "Invoice PDF failed: " + file, e);
            throw new IllegalStateException("No se pudo generar el PDF de la factura: " + e.getMessage(), e);
        }
    }

    /** Navy band with the school brand on the left and the invoice number on the right. */
    private PdfPTable header(Invoice invoice) {
        PdfPTable table = new PdfPTable(new float[]{58, 42});
        table.setWidthPercentage(100);

        PdfPCell brand = new PdfPCell();
        brand.setBackgroundColor(NAVY);
        brand.setBorder(Rectangle.NO_BORDER);
        brand.setPadding(14);
        Paragraph name = new Paragraph();
        name.add(new Chunk("Little ", BRAND));
        name.add(new Chunk("Horizons", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Font.NORMAL, GOLD)));
        brand.addElement(name);
        brand.addElement(new Paragraph(SchoolInfo.LEGAL_NAME, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Font.NORMAL, Color.WHITE)));
        brand.addElement(new Paragraph("NIT " + SchoolInfo.NIT + "  ·  " + SchoolInfo.TAX_REGIME, BRAND_SUB));
        brand.addElement(new Paragraph(SchoolInfo.ADDRESS, BRAND_SUB));
        brand.addElement(new Paragraph("Tel. " + SchoolInfo.PHONE + "  ·  " + SchoolInfo.EMAIL, BRAND_SUB));
        table.addCell(brand);

        PdfPCell box = new PdfPCell();
        box.setBackgroundColor(LIGHT);
        box.setBorder(Rectangle.NO_BORDER);
        box.setPadding(14);
        box.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph title = new Paragraph("FACTURA ELECTRÓNICA DE VENTA", TITLE);
        title.setAlignment(Element.ALIGN_RIGHT);
        Paragraph number = new Paragraph(invoice.getNumber(), NUMBER);
        number.setAlignment(Element.ALIGN_RIGHT);
        Paragraph date = new Paragraph("Fecha de emisión: " + Formats.date(invoice.getIssueDate()), VALUE);
        date.setAlignment(Element.ALIGN_RIGHT);
        box.addElement(title);
        box.addElement(number);
        box.addElement(date);
        table.addCell(box);

        PdfPCell accent = new PdfPCell(new Phrase(" "));
        accent.setColspan(2);
        accent.setFixedHeight(4);
        accent.setBackgroundColor(TEAL);
        accent.setBorder(Rectangle.NO_BORDER);
        table.addCell(accent);
        return table;
    }

    /** Payer (guardian) and student data side by side. */
    private PdfPTable parties(Guardian payer, Student student) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        PdfPCell left = card("ADQUIRIENTE (ACUDIENTE PAGADOR)");
        if (payer != null) {
            field(left, "Nombre", payer.getFullName());
            field(left, "Documento", payer.getDocumentType() + " " + payer.getDocumentNumber());
            field(left, "Teléfono", payer.getPhone());
            field(left, "Correo", payer.getEmail());
            field(left, "Dirección", payer.getAddress());
        } else {
            field(left, "Nombre", "Consumidor final");
        }
        PdfPCell right = card("ESTUDIANTE BENEFICIARIO");
        field(right, "Nombre", student.getFullName());
        field(right, "Documento", student.getDocumentType() + " " + student.getDocumentNumber());
        field(right, "Dirección", student.getAddress());
        table.addCell(left);
        table.addCell(right);
        return table;
    }

    private PdfPTable detail(InvoiceDocument data) {
        PdfPTable table = new PdfPTable(new float[]{52, 10, 19, 19});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        for (String column : new String[]{"Descripción", "Cant.", "Valor unitario", "Subtotal"}) {
            PdfPCell cell = new PdfPCell(new Phrase(column, HEADER));
            cell.setBackgroundColor(NAVY);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPadding(7);
            cell.setHorizontalAlignment(column.equals("Descripción") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        }
        boolean zebra = false;
        for (InvoiceLine line : data.lines()) {
            Color background = zebra ? LIGHT : Color.WHITE;
            table.addCell(row(line.description(), Element.ALIGN_LEFT, background));
            table.addCell(row(String.valueOf(line.quantity()), Element.ALIGN_RIGHT, background));
            table.addCell(row(Formats.money(line.unitPrice()), Element.ALIGN_RIGHT, background));
            table.addCell(row(Formats.money(line.subtotal()), Element.ALIGN_RIGHT, background));
            zebra = !zebra;
        }
        return table;
    }

    private PdfPTable totalsAndPayment(Invoice invoice, Payment payment) {
        PdfPTable outer = new PdfPTable(new float[]{55, 45});
        outer.setWidthPercentage(100);

        PdfPCell paymentCard = card("INFORMACIÓN DEL PAGO");
        field(paymentCard, "Medio de pago", payment.getMethod().getDbValue());
        field(paymentCard, "Recibo", payment.getReceiptNumber());
        field(paymentCard, "Fecha de pago", Formats.date(payment.getPaymentDate()));
        field(paymentCard, "Forma de pago", "Contado");
        outer.addCell(paymentCard);

        PdfPTable totals = new PdfPTable(new float[]{55, 45});
        totals.addCell(totalRow("Subtotal", false));
        totals.addCell(totalValue(Formats.money(invoice.getSubtotal()), false));
        totals.addCell(totalRow("IVA (19 %)", false));
        totals.addCell(totalValue(Formats.money(invoice.getTax()), false));
        totals.addCell(totalRow("TOTAL A PAGAR", true));
        totals.addCell(totalValue(Formats.money(invoice.getTotal()), true));
        PdfPCell wrapper = new PdfPCell(totals);
        wrapper.setBorder(Rectangle.NO_BORDER);
        wrapper.setPaddingLeft(10);
        outer.addCell(wrapper);
        return outer;
    }

    /** QR on the left, CUFE and DIAN resolution on the right. */
    private PdfPTable footer(InvoiceDocument data) throws IOException, DocumentException, WriterException {
        PdfPTable table = new PdfPTable(new float[]{22, 78});
        table.setWidthPercentage(100);

        Image qr = Image.getInstance(QrCodeGenerator.toPng(data.qrContent(), 220));
        qr.scaleAbsolute(105, 105);
        PdfPCell qrCell = new PdfPCell(qr, false);
        qrCell.setBorder(Rectangle.BOX);
        qrCell.setBorderColor(BORDER);
        qrCell.setPadding(6);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(qrCell);

        PdfPCell info = new PdfPCell();
        info.setBorder(Rectangle.BOX);
        info.setBorderColor(BORDER);
        info.setPadding(10);
        info.addElement(new Paragraph("CUFE", LABEL));
        info.addElement(new Paragraph(data.invoice().getCufe(), MONO));
        info.addElement(spacer(6));
        info.addElement(new Paragraph(SchoolInfo.RESOLUTION, SMALL));
        info.addElement(spacer(4));
        info.addElement(new Paragraph("Representación gráfica de la factura electrónica. Documento SIMULADO con fines "
                + "académicos, sin validez fiscal ante la DIAN. Servicios educativos excluidos de IVA (art. 476 E.T.).", SMALL));
        table.addCell(info);
        return table;
    }

    // ---------- small builders ----------

    private PdfPCell card(String title) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BORDER);
        cell.setPadding(10);
        Paragraph heading = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Font.NORMAL, TEAL));
        heading.setSpacingAfter(4);
        cell.addElement(heading);
        return cell;
    }

    private void field(PdfPCell card, String label, String value) {
        Paragraph line = new Paragraph();
        line.add(new Chunk(label + ":  ", LABEL));
        line.add(new Chunk(value == null || value.isBlank() ? "-" : value, VALUE));
        card.addElement(line);
    }

    private PdfPCell row(String text, int alignment, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(background);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER);
        cell.setPadding(7);
        return cell;
    }

    private PdfPCell totalRow(String label, boolean highlight) {
        PdfPCell cell = new PdfPCell(new Phrase(label, highlight ? TOTAL : LABEL));
        styleTotal(cell, highlight);
        return cell;
    }

    private PdfPCell totalValue(String value, boolean highlight) {
        PdfPCell cell = new PdfPCell(new Phrase(value, highlight ? TOTAL : VALUE));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        styleTotal(cell, highlight);
        return cell;
    }

    private void styleTotal(PdfPCell cell, boolean highlight) {
        cell.setPadding(7);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER);
        if (highlight) {
            cell.setBackgroundColor(TEAL);
            cell.setBorder(Rectangle.NO_BORDER);
        }
    }

    private Paragraph spacer(float height) {
        Paragraph paragraph = new Paragraph(" ");
        paragraph.setLeading(height);
        return paragraph;
    }
}
