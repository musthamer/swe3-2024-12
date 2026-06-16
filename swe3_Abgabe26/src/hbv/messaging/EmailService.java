package hbv.messaging;

public final class EmailService {

    private static final EmailSender SENDER =
        new LoggingEmailSender(new RedisEmailSender());

    private EmailService() {
    }

    public static void send(EmailMessage message) {
        SENDER.send(message);
    }
}
