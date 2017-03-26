package hoard.serialization;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import hoard.internal.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import static hoard.internal.IOUtils.UFT8;

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

  public GsonSerializer() {
    this.gson = new Gson();
  }

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
