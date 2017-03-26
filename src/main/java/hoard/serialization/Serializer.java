package hoard.serialization;

import hoard.Hoard;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * Specification that describes how {@link Hoard} should serialize objects.
 *
 * @since 1.0
 */
public interface Serializer {
  /**
   * Serialize an object to the output stream.
   *
   * @param type The type of the value being serialized.
   * @param value The value to serialize to outputStream.
   * @param outputStream The stream to serialize the object to.
   * @param <T> The type of the object passed in as value
   */
  <T> void serialize(Type type, T value, OutputStream outputStream) throws IOException;

  /**
   * De-serialize an object to the input stream.
   *
   * @param type The type of the object to de-serialize from the inputStream.
   * @param inputStream The stream to read the object from.
   * @param <T> The type of the object to return as.
   * @return The de-serialized value.
   */
  <T> T deserialize(Type type, InputStream inputStream) throws IOException;
}
