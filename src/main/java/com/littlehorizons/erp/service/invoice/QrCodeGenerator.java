package com.littlehorizons.erp.service.invoice;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Genera la imagen del código QR de la factura con la librería ZXing.
 */
public final class QrCodeGenerator {

    // Constructor privado: clase de utilidad
    private QrCodeGenerator() {
    }

    /**
     * Convierte un texto en un código QR.
     *
     * @param content texto que se codifica en el QR
     * @param size    ancho y alto en píxeles
     * @return los bytes de una imagen PNG
     */
    public static byte[] toPng(String content, int size) throws WriterException, IOException {
        // BitMatrix es la cuadrícula de puntos blancos y negros del QR; MARGIN 1 deja un borde pequeño
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size,
                Map.of(EncodeHintType.MARGIN, 1, EncodeHintType.CHARACTER_SET, "UTF-8"));
        // La imagen se escribe en memoria (no en un archivo) para insertarla directamente en el PDF
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        }
    }
}
