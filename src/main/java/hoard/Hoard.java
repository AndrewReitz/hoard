package hoard;

import hoard.serialization.ObjectStreamSerializer;
import hoard.serialization.Serializer;
import java.io.File;
import java.lang.reflect.Type;

/**
 * Factory for creating new {@link Depositor} or {@link ReactiveStreamDepositor}
 *
 * @since 1.0
 */
public class Hoard {

  private final File rootDirectory;
  private final Serializer serializer;

  /**
   * Builder for creating {@link Hoard}.
   *
   * @return a new builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  private Hoard(Builder builder) {
    this.rootDirectory = builder.rootDirectory;
    this.serializer = builder.serializer;
  }

  public <T> Depositor<T> createDepositor(String key, Class<T> typeToSave) {
    return createDepositor(key, (Type) typeToSave);
  }

  public <T> Depositor<T> createDepositor(String key, Type type) {
    if (key == null) throw new NullPointerException("key == null");
    if (type == null) throw new NullPointerException("type == null");
    return new DefaultDepositor<T>(serializer, rootDirectory, key, type);
  }

  public <T> ReactiveStreamDepositor<T> createReactiveDepositor(String key, Class<T> type) {
    return createReactiveDepositor(key, (Type) type);
  }

  public <T> ReactiveStreamDepositor<T> createReactiveDepositor(String key, Type type) {
    Depositor<T> depositor = createDepositor(key, type);
    return new DefaultReactiveStreamDepositor<T>(depositor);
  }

  public static class Builder {
    private File rootDirectory;
    private Serializer serializer = new ObjectStreamSerializer();

    /**
     * Set the root directory for {@link Hoard} to save values to. This directory should ONLY
     * be utilized for {@link Hoard}.
     *
     * @param rootDirectory The directory to save values to.
     * @return builder for chaining methods.
     * @throws NullPointerException if rootDirectory is null.
     * @throws IllegalArgumentException if rootDirectory is not a directory or does not exist and
     * cannot be created.
     */
    public Builder rootDirectory(File rootDirectory) {
      if (rootDirectory == null) throw new NullPointerException("rootDirectory == null");
      if (!rootDirectory.isDirectory()) {
        throw new IllegalArgumentException("rootDirectory is not a directory");
      }
      if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
        throw new IllegalArgumentException("rootDirectory does not exist, and can not be created");
      }

      this.rootDirectory = rootDirectory;
      return this;
    }

    /**
     * Set the serialization strategy for {@link Hoard}.
     * The default is {@link ObjectStreamSerializer}.
     *
     * @param serializer the serializer to be used by {@link Hoard}.
     * @return builder for chaining methods.
     * @throws NullPointerException if the serializer is null.
     */
    public Builder serialzationAdapter(Serializer serializer) {
      if (serializer == null) {
        throw new NullPointerException("serializer == null");
      }

      this.serializer = serializer;
      return this;
    }

    /**
     * Creates a new instance of {@link Hoard}
     * @throws IllegalStateException if a root direcotry has not been provided.
     */
    public Hoard build() {
      if (rootDirectory == null) {
        throw new IllegalStateException("rootDirectory is not set");
      }

      return new Hoard(this);
    }
  }
}
