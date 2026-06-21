package hbv.messaging;

import java.util.Base64;

import org.json.JSONObject;

import hbv.web.JedisAdapter;
import redis.clients.jedis.Jedis;

public final class RedisEmailSender {

    private static final int SESSION_EMAIL_TTL_SECONDS = 3600;

    private RedisEmailSender() {
    }

    public static String emailsKey(String sessionId) {
        return "emails:" + sessionId;
    }

    public static void send(EmailMessage message, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session-ID fehlt beim Speichern der E-Mail.");
        }

        System.out.println("[Email] Sende an: " + message.getTo() + ", Betreff: " + message.getSubject());

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

            String key = emailsKey(sessionId);
            jedis.rpush(key, emailJson.toString());
            jedis.expire(key, SESSION_EMAIL_TTL_SECONDS);
        } finally {
            if (jedis != null) {
                JedisAdapter.releaseJedis(jedis);
            }
        }
    }

    public static void clearSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        Jedis jedis = null;
        try {
            jedis = JedisAdapter.getJedis();
            jedis.del(emailsKey(sessionId));
        } finally {
            if (jedis != null) {
                JedisAdapter.releaseJedis(jedis);
            }
        }
    }
}
