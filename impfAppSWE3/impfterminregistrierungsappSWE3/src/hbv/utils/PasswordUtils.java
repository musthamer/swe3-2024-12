package hbv.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

  private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
  private static final int ITERATIONS = 210_000;
  private static final int KEY_LENGTH = 512;
  private static final int SALT_LENGTH = 8;

  public static String hashPassword(String password) throws Exception {
    byte[] salt = generateSalt();
    byte[] hash = hashPassword(password, salt);
    return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
  }

  public static byte[] generateSalt() throws NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstanceStrong();
    byte[] salt = new byte[SALT_LENGTH];
    random.nextBytes(salt);
    return salt;
  }

  static byte[] hashPassword(String password, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
      SecretKey key = factory.generateSecret(spec);
      return key.getEncoded();
    } finally {
      spec.clearPassword();
    }
  }

  public static boolean verifyPassword(String password, String storedHash) {
    try {
      String[] parts = storedHash.split(":");
      if (parts.length != 2 || !isHexSalt(parts[0]) || !isHex(parts[1])) {
        return false;
      }
      byte[] salt = HexFormat.of().parseHex(parts[0]);
      byte[] hash = HexFormat.of().parseHex(parts[1]);
      byte[] testHash = hashPassword(password, salt);
      return Arrays.equals(hash, testHash);
    } catch (Exception e) {
      System.err.println("Fehler bei der Passwort-Verifikation: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private static boolean isHexSalt(String value) {
    return value.length() == SALT_LENGTH * 2 && isHex(value);
  }

  private static boolean isHex(String value) {
    return value.matches("[0-9a-fA-F]+");
  }
}
