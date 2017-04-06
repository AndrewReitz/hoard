package io.github.roguesdev.hoard;

import io.github.roguesdev.hoard.serialization.ObjectStreamSerializer;
import io.github.roguesdev.hoard.serialization.Serializer;
import java.io.File;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Factory for creating new {@link Depositor} or {@link ReactiveStreamDepositor}. Also provides
 * utilities for doing large operations for clearing all values, and retrieving all values.
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

  /** Deletes all values stored by {@link Hoard}. */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void deleteAll() {
    File[] files = rootDirectory.listFiles();
    if (files == null) return;
    for (File file : files) {
      file.delete();
    }
  }

  /**
   * Retrieve all values stored in {@link Hoard}. This will pull all values into memory and could
   * possibly cause problems. It is recommended to use {@link #retrieveAllReactive()} to avoid
   * heavy memory usage.
   * This can be useful for creating migrations.
   *
   * @return Map of all key-values stored.
   */
  public Map<String, Object> retrieveAll() {
    Map<String, Object> returnValues = new LinkedHashMap<String, Object>();
    String[] list = rootDirectory.list();
    if (list == null) return returnValues;

    for (String name : list) {
      Object retrieve = createDepositor(name, Object.class).retrieve();
      returnValues.put(name, retrieve);
    }

    return returnValues;
  }

  /**
   * Same as {@link #deleteAll()} but wrapped in a {@link Publisher}.
   *
   * @return Publisher that when subscribed will preform the delete operation.
   */
  public Publisher<Void> deleteAllReactive() {
    return new Publisher<Void>() {
      @Override public void subscribe(final Subscriber<? super Void> s) {
        s.onSubscribe(new Subscription() {
          volatile boolean canceled = false;

          @SuppressWarnings("ResultOfMethodCallIgnored")
          @Override public void request(long n) {
            try {
              File[] files = rootDirectory.listFiles();
              if (files == null) return;
              for (File file : files) {
                file.delete();
              }
              if (canceled) return;
              s.onComplete();
            } catch (Exception e) {
              if (canceled) return;
              s.onError(e);
            }
          }

          @Override public void cancel() {
            canceled = true;
          }
        });
      }
    };
  }

  /**
   * Same as {@link #retrieveAll()} but wrapped in a {@link Publisher}.
   *
   * @return Publisher that when subscribed will retrieve the key value pairs stored in
   * {@link Hoard} and provide them as a {@link Pair}
   */
  public Publisher<Pair> retrieveAllReactive() {
    return new Publisher<Pair>() {
      @Override public void subscribe(final Subscriber<? super Pair> s) {
        s.onSubscribe(new Subscription() {

          volatile boolean canceled = false;

          @Override public void request(long n) {
            String[] list = rootDirectory.list();
            if (list == null) {
              if (canceled) return;
              s.onComplete();
              return;
            }

            for (String name : list) {
              Object retrieve = createDepositor(name, Object.class).retrieve();
              Pair value = new Pair(name, retrieve);
              if (canceled) return;
              s.onNext(value);
            }

            if (canceled) return;
            s.onComplete();
          }

          @Override public void cancel() {
            canceled = true;
          }
        });
      }
    };
  }

  /**
   * A key value pair.
   */
  public static class Pair {
    private final String key;
    private final Object value;

    public Pair(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    public String key() {
      return key;
    }

    @SuppressWarnings("unchecked")
    public <T> T value() {
      return (T) value;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Pair pair = (Pair) o;

      if (!key.equals(pair.key)) return false;
      return value != null ? value.equals(pair.value) : pair.value == null;
    }

    @Override public int hashCode() {
      int result = key.hashCode();
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
    }

    @Override public String toString() {
      return "Pair{" +
        "key='" + key + '\'' +
        ", value=" + value +
        '}';
    }
  }

  /**
   * Builder for creating {@link Hoard}.
   */
  public static class Builder {
    private File rootDirectory;
    private Serializer serializer = new ObjectStreamSerializer();

    /**
     * Set the root directory for {@link Hoard} to store values to. This directory should ONLY
     * be utilized for {@link Hoard}.
     *
     * @param rootDirectory The directory to store values to.
     * @return builder for chaining methods.
     * @throws NullPointerException if rootDirectory is null.
     * @throws IllegalArgumentException if rootDirectory is not a directory or does not exist and
     * cannot be created.
     */
    public Builder rootDirectory(File rootDirectory) {
      if (rootDirectory == null) throw new NullPointerException("rootDirectory == null");
      if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
        throw new IllegalArgumentException("rootDirectory does not exist, and can not be created");
      }
      if (!rootDirectory.isDirectory()) {
        throw new IllegalArgumentException("rootDirectory is not a directory");
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
     *
     * @return a new instance of Hoard created with the values set in the builder.
     * @throws IllegalStateException if a rootDirectory has not been provided.
     */
    public Hoard build() {
      if (rootDirectory == null) {
        throw new IllegalStateException("rootDirectory is not set");
      }

      return new Hoard(this);
    }
  }
}
