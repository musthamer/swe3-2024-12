package hbv.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
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

    // 🔹 Schritt 1: Setze die Basisinformationen
    public BookingDocumentBuilder setBookingId(String bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public BookingDocumentBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    // 🔹 Schritt 2: QR-Code generieren und in Redis speichern
    public BookingDocumentBuilder generateQRCode() throws Exception {
        String qrData = "http://localhost:8080/impfregistrierungsanwendung/verify?code=" + bookingId;
        
        BitMatrix matrix = new MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 200, 200);
        qrOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", qrOutputStream);

        // 🔹 In Redis speichern
        try (Jedis jedis = RedisConfig.getConnection()) {
            jedis.set(("qr:" + bookingId).getBytes(), qrOutputStream.toByteArray());
        }

        return this;
    }

    // 🔹 Schritt 3: PDF-Dokument erstellen und in Redis speichern
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

        // 🔹 In Redis speichern
        try (Jedis jedis = RedisConfig.getConnection()) {
            jedis.set(("pdf:" + bookingId).getBytes(), pdfOutputStream.toByteArray());
        }

        return this;
    }

    // 🔹 Schritt 4: Ergebnis abrufen (für Konsistenz beibehalten)
    public BookingDocumentResult build() {
        return new BookingDocumentResult(null, null);  // Kein Dateipfad, da die Daten in Redis gespeichert werden
    }
}

