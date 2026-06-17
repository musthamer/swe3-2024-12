package hbv.messaging;

import hbv.web.JedisAdapter;
import java.util.Base64;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

public class RedisEmailSender implements EmailSender {

  @Override
  public void send(EmailMessage message) {
    Jedis jedis = null;
    try {
      jedis = JedisAdapter.getJedis();

      JSONObject emailJson = new JSONObject();
      emailJson.put("to", message.getTo());
      emailJson.put("subject", message.getSubject());
      emailJson.put("body", message.getBody());

      if (message.hasAttachment()) {
        JSONObject attachment = new JSONObject();
        attachment.put("filename", message.getAttachmentName());
        attachment.put("content", Base64.getEncoder().encodeToString(message.getAttachment()));
        emailJson.put("attachment", attachment);
      }

      jedis.rpush("emails", emailJson.toString());
    } finally {
      if (jedis != null) {
        JedisAdapter.releaseJedis(jedis);
      }
    }
  }
}
