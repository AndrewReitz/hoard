package io.github.roguesdev.hoard.serialization;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.github.roguesdev.hoard.internal.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import static io.github.roguesdev.hoard.internal.IOUtils.UFT8;

/**
 * Adapter for saving values as json using Gson.
 *
 * @since 1.0
 */
public class GsonSerializer implements Serializer {

  static {
    try {
      Class.forName("com.google.gson.Gson", false, GsonSerializer.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Gson is not found on the classpath. Please include the "
        + "Gson library in order to use GsonSerializer");
    }
  }

  private final Gson gson;

  /**
   * Default constructor.
   * Creates a {@link Gson} instance using the emptry constructor. If an instance
   * that supports custom serialization is required see {@link #GsonSerializer(Gson)}
   */
  public GsonSerializer() {
    this(new Gson());
  }

  /**
   * Build a new instance of {@link GsonSerializer} with a configured version of gson.
   *
   * @param gson instance of {@link Gson} that has been setup by the user for serializing objects.
   */
  public GsonSerializer(Gson gson) {
    if (gson == null) throw new NullPointerException("gson can not be null");
    this.gson = gson;
  }

  @Override public <T> void serialize(Type type, T value, OutputStream outputStream)
      throws IOException {
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, UFT8);
    JsonWriter jsonWriter = new JsonWriter(outputStreamWriter);

    gson.toJson(value, type, jsonWriter);

    jsonWriter.close();
    outputStreamWriter.close();
  }

  @Override public <T> T deserialize(Type type, InputStream inputStream) throws IOException {
    InputStreamReader inputStreamReader = null;
    try {
      inputStreamReader = new InputStreamReader(inputStream, UFT8);
      return gson.fromJson(inputStreamReader, type);
    } finally {
      IOUtils.close(inputStreamReader);
    }
  }
}
