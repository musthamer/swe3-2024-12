package hbv.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import redis.clients.jedis.Jedis;
import hbv.service.RedisConfig;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

public class BookingDocumentBuilder {

    private String bookingId;
    private String email;
    private ByteArrayOutputStream pdfOutputStream;
    private ByteArrayOutputStream qrOutputStream;

    // Setzen der Basisinformationen
    public BookingDocumentBuilder setBookingId(String bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public BookingDocumentBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    // QR-Code generieren und in Redis speichern
    public BookingDocumentBuilder generateQRCode() throws Exception {
        String qrData = "http://localhost:8080/impfregistrierungsanwendung/verify?code=" + bookingId;

        BitMatrix matrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 200, 200);
        qrOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", qrOutputStream);

        try (Jedis jedis = RedisConfig.getConnection()) {
            jedis.set(("qr:" + bookingId).getBytes(), qrOutputStream.toByteArray());
        }

        return this;
    }

    // PDF-Dokument erstellen und in Redis speichern
    public BookingDocumentBuilder generatePdf() throws IOException {
        pdfOutputStream = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText("Impfbestätigung");
            contentStream.newLineAtOffset(0, -30);
            contentStream.showText("Buchungs-ID: " + bookingId);
            contentStream.newLineAtOffset(0, -30);
            contentStream.showText("E-Mail: " + email);
            contentStream.endText();

            PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, qrOutputStream.toByteArray(), "qr-code");
            contentStream.drawImage(qrImage, 50, 400, 200, 200);

            contentStream.close();
            document.save(pdfOutputStream);
        }

        try (Jedis jedis = RedisConfig.getConnection()) {
            jedis.set(("pdf:" + bookingId).getBytes(), pdfOutputStream.toByteArray());
        }

        return this;
    }

    public BookingDocumentResult build() {
        return new BookingDocumentResult(null, null);  // Kein Dateipfad, da die Daten in Redis gespeichert werden
    }
}

