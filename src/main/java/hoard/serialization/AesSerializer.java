package hoard.serialization;

import hoard.internal.ExceptionUtil;
import hoard.internal.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation of {@link Serializer} that encrypts objects before
 * saving. This implementation uses {@link ObjectOutputStream} to serialize the data
 * and thus can only operate on objects that are {@link Serializable}.
 *
 * This adapter is written to be available for use with both Android and JVM platforms. If
 * exceptions occur, you may need to use <a href="https://www.bouncycastle.org/latest_releases.html">
 *   Bouncy Castle and or install</a>
 * <a href="http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html">
 *   Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy</a>.
 *
 * @since 1.0
 */
public class AesSerializer implements Serializer {

  private static final String ENCRYPTION_ALGORITHM = "AES";
  private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
  private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

  private static final int KEY_SIZE = 256;
  private static final int DERIVATION_ITERATION_COUNT = 1024;

  private static final byte[] IV = {16, 74, 71, -80, 32, 101, -47, 72, 117, -14, 0, -29, 70, 65, -12, 74};

  private final Cipher encryptCipher;
  private final Cipher decryptCipher;

  /**
   * Constructor for initializing AesSerializer.
   *
   * @param key The key or password to use for encrypting objects before saving them.
   * @param salt The salt to salt the password with.
   * @see <a href="https://docs.oracle.com/javase/7/docs/api/javax/crypto/spec/PBEKeySpec.html">PBEKeySpec</a>
   * @see <a href="https://javarevisited.blogspot.com/2012/03/why-character-array-is-better-than.html">
   *   Why character array is better than String for Storing password in Java</a>
   */
  public AesSerializer(char[] key, byte[] salt) {
    if (key == null) throw new NullPointerException("key == null");
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
      KeySpec keySpec = new PBEKeySpec(key, salt, DERIVATION_ITERATION_COUNT, KEY_SIZE);
      SecretKey tmp = factory.generateSecret(keySpec);
      SecretKey secret = new SecretKeySpec(tmp.getEncoded(), ENCRYPTION_ALGORITHM);

      IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);

      encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
      encryptCipher.init(Cipher.ENCRYPT_MODE, secret, ivParameterSpec);

      decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
      decryptCipher.init(Cipher.DECRYPT_MODE, secret, ivParameterSpec);

    } catch (Exception e) {
      ExceptionUtil.sneakyThrow(e);
      throw new IllegalStateException();
    }
  }

  @Override public final <T> void serialize(Type type, T value, OutputStream outputStream)
      throws IOException {

    CipherOutputStream cipherOutputStream = null;
    ObjectOutputStream objectOutputStream = null;

    try {
      cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
      objectOutputStream = new ObjectOutputStream(outputStream);

      objectOutputStream.writeObject(value);

    } finally {
      IOUtils.close(cipherOutputStream, objectOutputStream);
    }
  }

  @SuppressWarnings("unchecked")
  @Override public final <T> T deserialize(Type type, InputStream inputStream) throws IOException {
    ObjectInputStream objectInputStream = null;
    CipherInputStream cipherInputStream = null;

    try {
      cipherInputStream = new CipherInputStream(inputStream, decryptCipher);
      objectInputStream = new ObjectInputStream(inputStream);

      return (T) objectInputStream.readObject();

    } catch (Exception e) {
      ExceptionUtil.sneakyThrow(e);
      return null;
    } finally {
      IOUtils.close(objectInputStream, cipherInputStream);
    }
  }
}
