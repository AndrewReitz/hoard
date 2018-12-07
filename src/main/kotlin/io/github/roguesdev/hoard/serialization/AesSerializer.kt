package io.github.roguesdev.hoard.serialization

import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serializable
import java.lang.reflect.Type
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val ENCRYPTION_ALGORITHM = "AES"
private const val KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC"
private const val CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"

private const val KEY_SIZE = 256
private const val DERIVATION_ITERATION_COUNT = 1024

private val IV = byteArrayOf(16, 74, 71, -80, 32, 101, -47, 72, 117, -14, 0, -29, 70, 65, -12, 74)

/**
 * An implementation of [Serializer] that encrypts objects before
 * saving. This implementation uses [ObjectOutputStream] to serialize the data
 * and thus can only operate on objects that are [Serializable].
 *
 * This adapter is written to be available for use with both Android and JVM platforms. If
 * exceptions occur, you may need to use [
 * Bouncy Castle and or install](https://www.bouncycastle.org/latest_releases.html)
 * [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html).
 *
 *
 * @param key The key or password to use for encrypting objects before saving them.
 * @param salt The salt to salt the password with.
 * @see [PBEKeySpec](https://docs.oracle.com/javase/7/docs/api/javax/crypto/spec/PBEKeySpec.html)
 *
 * @see [Why character array is better than String for Storing password in Java](https://javarevisited.blogspot.com/2012/03/why-character-array-is-better-than.html)
 *
 * @since 1.0
 */
class AesSerializer(key: CharArray?, salt: ByteArray) : Serializer {

  private val encryptCipher: Cipher
  private val decryptCipher: Cipher

  init {
    val factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM)
    val keySpec = PBEKeySpec(key, salt, DERIVATION_ITERATION_COUNT, KEY_SIZE)
    val tmp = factory.generateSecret(keySpec)
    val secret = SecretKeySpec(tmp.encoded, ENCRYPTION_ALGORITHM)

    val ivParameterSpec = IvParameterSpec(IV)

    encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM)
    encryptCipher.init(Cipher.ENCRYPT_MODE, secret, ivParameterSpec)

    decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM)
    decryptCipher.init(Cipher.DECRYPT_MODE, secret, ivParameterSpec)
  }

  @Throws(IOException::class)
  override fun <T> serialize(type: Type, value: T, outputStream: OutputStream) {
    CipherOutputStream(outputStream, encryptCipher).use { cipherStream ->
      ObjectOutputStream(cipherStream).use {
        it.writeObject(value)
      }
    }
  }

  @Throws(IOException::class)
  override fun <T> deserialize(type: Type, inputStream: InputStream): T =
    CipherInputStream(inputStream, decryptCipher).use { cipherStream ->
      ObjectInputStream(cipherStream).use {
        @Suppress("UNCHECKED_CAST")
        it.readObject() as T
      }
    }
}
