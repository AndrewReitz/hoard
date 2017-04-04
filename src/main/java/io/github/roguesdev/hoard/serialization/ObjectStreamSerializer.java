package io.github.roguesdev.hoard.serialization;

import io.github.roguesdev.hoard.Hoard;
import io.github.roguesdev.hoard.internal.ExceptionUtil;
import io.github.roguesdev.hoard.internal.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Uses {@link ObjectOutputStream} and {@link ObjectInputStream} to store objects. All
 * objects to be saved must implement {@link Serializable}.
 * This is the default implementation for {@link Hoard}.
 *
 * @since 1.0
 */
public class ObjectStreamSerializer implements Serializer {

  @Override public <T> void serialize(Type type, T value, OutputStream outputStream) throws IOException {
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
    objectOutputStream.writeObject(value);
    objectOutputStream.close();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialize(Type type, InputStream inputStream) throws IOException {
    ObjectInputStream objectInputStream = null;
    try {
      objectInputStream = new ObjectInputStream(inputStream);
      Object object = objectInputStream.readObject();
      return (T) object;
    } catch (ClassNotFoundException e) {
      // shouldn't happen but throw it up if it does
      ExceptionUtil.sneakyThrow(e);
      return null;
    } finally {
      IOUtils.close(objectInputStream);
    }
  }
}
