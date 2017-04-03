package hoard.serialization;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import hoard.internal.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Adapter for saving values as json using Moshi.
 *
 * @since 1.0
 */
public class MoshiSerializer implements Serializer {

  static {
    try {
      Class.forName("com.squareup.moshi.Moshi", false, MoshiSerializer.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Moshi is not found on the classpath. Please include the "
        + "Moshi library to use the MoshiSerializer");
    }
  }

  private final Moshi moshi;

  public MoshiSerializer() {
    this(new Moshi.Builder().build());
  }

  public MoshiSerializer(Moshi moshi) {
    if (moshi == null) throw new NullPointerException("moshi == null");
    this.moshi = moshi;
  }

  @Override
  public <T> void serialize(Type type, T value, OutputStream outputStream) throws IOException {
    JsonAdapter<Object> adapter = moshi.adapter(type); // no need to cache moshi does this for us.
    BufferedSink buffer = Okio.buffer(Okio.sink(outputStream));
    adapter.toJson(buffer, value);
    buffer.close();
  }

  @Override
  public <T> T deserialize(Type type, InputStream inputStream) throws IOException {
    BufferedSource buffer = null;
    try {
      buffer = Okio.buffer(Okio.source(inputStream));
      JsonAdapter<T> adapter = moshi.adapter(type);
      return adapter.fromJson(buffer);
    } finally {
      IOUtils.close(buffer);
    }
  }
}
