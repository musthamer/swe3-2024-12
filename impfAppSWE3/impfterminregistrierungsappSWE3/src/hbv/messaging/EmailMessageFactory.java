package hbv.messaging;

import java.util.Date;

public final class EmailMessageFactory {

  private EmailMessageFactory() {}

  public static EmailMessage registration(String email, String firstName, String activationUrl) {
    String subject = "Aktivieren Sie Ihren Account für die Impfterminbuchung";
    String body =
        "Hallo "
            + firstName
            + ",\n\n"
            + "vielen Dank für Ihre Registrierung. Bitte aktivieren Sie Ihren Account mit folgendem"
            + " Link:\n\n"
            + activationUrl
            + "\n\n"
            + "Der Link ist 24 Stunden gültig.\n\n"
            + "Mit freundlichen Grüßen\n"
            + "Ihr Impfterminbuchungsteam";
    return new EmailMessage(email, subject, body);
  }

  public static EmailMessage activation(String email, String firstName) {
    String subject = "Ihr Account für die Impfterminbuchung wurde aktiviert";
    String body =
        "Hallo "
            + firstName
            + ",\n\n"
            + "Ihr Account für die Impfterminbuchung wurde erfolgreich aktiviert.\n\n"
            + "Sie können sich jetzt anmelden und Impftermine buchen.\n\n"
            + "Mit freundlichen Grüßen,\n"
            + "Ihr Impfterminbuchungsteam";
    return new EmailMessage(email, subject, body);
  }

  public static EmailMessage passwordReset(String email, String firstName, String resetUrl) {
    String subject = "Passwort zurücksetzen für die Impfterminbuchung";
    String body =
        "Hallo "
            + firstName
            + ",\n\n"
            + "Sie haben eine Anfrage zum Zurücksetzen Ihres Passworts gestellt. "
            + "Bitte klicken Sie auf den folgenden Link, um Ihr Passwort zurückzusetzen:\n\n"
            + resetUrl
            + "\n\n"
            + "Der Link ist 1 Stunde gültig.\n\n"
            + "Falls Sie keine Anfrage gestellt haben, können Sie diese E-Mail ignorieren.\n\n"
            + "Mit freundlichen Grüßen,\n"
            + "Ihr Impfterminbuchungsteam";
    return new EmailMessage(email, subject, body);
  }

  public static EmailMessage bookingConfirmation(
      String email,
      String personName,
      Date appointmentDate,
      String vaccinationCenter,
      String vaccineType,
      byte[] pdfData) {

    String subject = "Ihre Impfterminbestätigung";
    String body =
        "Sehr geehrte(r) "
            + personName
            + ",\n\n"
            + "vielen Dank für Ihre Buchung. Ihr Impftermin wurde erfolgreich bestätigt.\n\n"
            + "Termin: "
            + appointmentDate
            + "\n"
            + "Impfzentrum: "
            + vaccinationCenter
            + "\n"
            + "Impfstoff: "
            + vaccineType
            + "\n\n"
            + "Im Anhang finden Sie Ihre Terminbestätigung als PDF.\n\n"
            + "Bitte bringen Sie einen gültigen Ausweis mit und erscheinen Sie pünktlich zum"
            + " Termin.\n\n"
            + "Mit freundlichen Grüßen,\n"
            + "Ihr Impfterminbuchungsteam";

    return new EmailMessage(email, subject, body, pdfData, "impftermin.pdf");
  }
}
