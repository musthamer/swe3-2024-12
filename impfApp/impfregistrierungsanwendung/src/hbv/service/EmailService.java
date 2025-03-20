package hbv.service;

import java.nio.file.Path;
import redis.clients.jedis.Jedis;
import hbv.service.RedisConfig;

public class EmailService {

    private static EmailService instance;

    // Singleton-Pattern für die EmailService-Instanz
    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    // 🔹 Alte Methode entfernen (da PDF und QR in Redis gespeichert werden)

    // 🔹 Neue Methode zur E-Mail-Generierung mit Byte-Daten
    public void generateBookingEmailWithAttachments(String recipient, byte[] pdfData, byte[] qrData, String bookingId) throws Exception {
        String emailContent = """
            =======================================
            📧 SIMULIERTE E-MAIL
            Von: impfzentrum@test.com
            An: %s
            Betreff: Ihre Impfbestätigung
            Inhalt:
            Ihre Buchung war erfolgreich.
            Buchungs-ID: %s
            =======================================
            """.formatted(recipient, bookingId);

        System.out.println(emailContent);

        // 🔹 E-Mail-Log in Redis speichern
        try (Jedis jedis = RedisConfig.getConnection()) {
            jedis.set(("email_log:" + bookingId).getBytes(), emailContent.getBytes());
        }
    }
}

