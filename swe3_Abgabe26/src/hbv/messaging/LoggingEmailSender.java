package hbv.messaging;

public class LoggingEmailSender implements EmailSender {

    private final EmailSender delegate;

    public LoggingEmailSender(EmailSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(EmailMessage message) {
        System.out.println("[Email] Sende an: " + message.getTo() + ", Betreff: " + message.getSubject());
        delegate.send(message);
        System.out.println("[Email] Erfolgreich gespeichert.");
    }
}
