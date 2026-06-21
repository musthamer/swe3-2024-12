package hbv.web;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class PDFGenerator {

    public static byte[] generateVaccinationConfirmation(
            String name,
            Date appointmentDate,
            String vaccinationCenter,
            String vaccineType,
            int bookingId,
            String baseUrl,
            String webapp) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont titleFont = PDType1Font.HELVETICA_BOLD;
            PDFont textFont = PDType1Font.HELVETICA;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(titleFont, 20);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Impfterminbestätigung");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(textFont, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Name: " + name);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Termin: " + dateFormat.format(appointmentDate));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Impfzentrum: " + vaccinationCenter);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Impfstoff: " + vaccineType);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Buchungs-ID: " + bookingId);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(textFont, 12);
                contentStream.newLineAtOffset(50, 580);
                contentStream.showText("QR-Code für Ihren Impftermin:");
                contentStream.endText();

                try {
                    String qrCodeUrl = baseUrl + "/" + webapp
                        + "/admin/dashboard.html?tab=tabTermine&id=" + bookingId;

                    BufferedImage qrCodeImage = generateQRCode(qrCodeUrl, 200, 200);

                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrCodeImage);
                    contentStream.drawImage(pdImage, 50, 400, 200, 200);

                    contentStream.beginText();
                    contentStream.setFont(textFont, 10);
                    contentStream.newLineAtOffset(50, 380);
                    contentStream.showText("Bitte zeigen Sie diesen QR-Code bei Ihrer Ankunft im Impfzentrum vor.");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(textFont, 8);
                    contentStream.newLineAtOffset(50, 365);
                    contentStream.showText("URL: " + qrCodeUrl);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(textFont, 10);
                    contentStream.newLineAtOffset(50, 50);
                    contentStream.showText("Bitte erscheinen Sie pünktlich und bringen Sie einen gültigen Ausweis mit.");
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Bei Verhinderung bitten wir um rechtzeitige Absage.");
                    contentStream.endText();

                } catch (WriterException e) {
                    contentStream.beginText();
                    contentStream.setFont(textFont, 10);
                    contentStream.newLineAtOffset(50, 500);
                    contentStream.showText("QR-Code konnte nicht generiert werden: " + e.getMessage());
                    contentStream.endText();
                }
            }

            document.save(baos);
        }

        return baos.toByteArray();
    }

    private static BufferedImage generateQRCode(String data, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

}
