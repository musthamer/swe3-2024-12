package hbv.utils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

  private static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA1";
  private static final int DEFAULT_ITERATIONS = 65536;
  private static final int DEFAULT_KEY_LENGTH = 128;

  public static String hashPassword(String password) throws Exception {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);

    PBEKeySpec spec =
        new PBEKeySpec(password.toCharArray(), salt, DEFAULT_ITERATIONS, DEFAULT_KEY_LENGTH);
    SecretKeyFactory factory = SecretKeyFactory.getInstance(DEFAULT_ALGORITHM);
    byte[] hash = factory.generateSecret(spec).getEncoded();

    String saltString = Base64.getEncoder().encodeToString(salt);
    String hashString = Base64.getEncoder().encodeToString(hash);

    return saltString + ":" + hashString;
  }

  public static boolean verifyPassword(String password, String storedHash) {
    try {
      String[] parts = storedHash.split(":");
      if (parts.length < 2) {
        return false;
      }

      byte[] salt = Base64.getDecoder().decode(parts[0]);
      byte[] hash = Base64.getDecoder().decode(parts[parts.length - 1]);

      String algorithm = DEFAULT_ALGORITHM;
      int iterations = DEFAULT_ITERATIONS;
      int keyLength = DEFAULT_KEY_LENGTH;

      if (parts.length >= 3) {
        algorithm = parts[0];
        iterations = Integer.parseInt(parts[1]);
        keyLength = Integer.parseInt(parts[2]);
        salt = Base64.getDecoder().decode(parts[3]);
        hash = Base64.getDecoder().decode(parts[4]);
      }

      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
      byte[] testHash = skf.generateSecret(spec).getEncoded();

      return Arrays.equals(hash, testHash);
    } catch (Exception e) {
      System.err.println("Fehler bei der Passwort-Verifikation: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}
