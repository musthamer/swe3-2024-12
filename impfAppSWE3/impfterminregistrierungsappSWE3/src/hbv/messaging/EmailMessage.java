package hbv.messaging;

public class EmailMessage {
  private final String to;
  private final String subject;
  private final String body;
  private final byte[] attachment;
  private final String attachmentName;

  public EmailMessage(String to, String subject, String body) {
    this(to, subject, body, null, null);
  }

  public EmailMessage(
      String to, String subject, String body, byte[] attachment, String attachmentName) {
    this.to = to;
    this.subject = subject;
    this.body = body;
    this.attachment = attachment;
    this.attachmentName = attachmentName;
  }

  public String getTo() {
    return to;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public byte[] getAttachment() {
    return attachment;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public boolean hasAttachment() {
    return attachment != null;
  }
}
