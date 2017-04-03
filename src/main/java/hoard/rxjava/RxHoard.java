package hoard.rxjava;

import hoard.Depositor;
import hoard.Hoard;
import hoard.ReactiveStreamDepositor;
import java.lang.reflect.Type;
import java.util.Map;
import rx.Observable;
import rx.RxReactiveStreams;

/**
 * Wrapper for {@link Hoard} that produces {@link Depositor}s that can be used with RxJava 1.x
 *
 * @since 1.0
 */
public class RxHoard {

  static {
    ClassLoader classLoader = RxHoard.class.getClassLoader();
    try {
      Class.forName("rx.Observable", false, classLoader);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("RxJava 1.x is not found on the classpath. Please include the "
        + "RxJava library in order to use hoard.rxjava.RxHoard");
    }

    try {
      Class.forName("rx.RxReactiveStreams", false, classLoader);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("RxReactiveStreams is not found on the classpath. Please "
        + "include the rxjava-reactive-streams library in order to use hoard.rxjava.RxHoard");
    }
  }

  private final Hoard hoard;

  public RxHoard(Hoard hoard) {
    if (hoard == null) throw new NullPointerException("hoard == null");
    this.hoard = hoard;
  }

  public <T> RxDepositor<T> createDepositor(String key, Class<T> typeToSave) {
    return createDepositor(key, (Type) typeToSave);
  }

  public <T> RxDepositor<T> createDepositor(String key, Type type) {
    if (key == null) throw new NullPointerException("key == null");
    if (type == null) throw new NullPointerException("type == null");
    ReactiveStreamDepositor<T> reactiveDepositor = hoard.createReactiveDepositor(key, type);
    return new DefaultRxDepositor<T>(reactiveDepositor);
  }

  /** Deletes all values stored by {@link Hoard}. */
  public void deleteAll() {
    hoard.deleteAll();
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
    return hoard.retrieveAll();
  }

  /**
   * Same as {@link #deleteAll()} but wrapped in a {@link Observable}.
   *
   * @return Observable that when subscribed will preform the delete operation.
   */
  public Observable<Void> deleteAllReactive() {
    return RxReactiveStreams.toObservable(hoard.deleteAllReactive());
  }

  /**
   * Same as {@link #retrieveAll()} but wrapped in a {@link Observable}.
   *
   * @return Observable that when subscribed will retrieve the key value pairs stored in
   * {@link Hoard} and provide them as a {@link Hoard.Pair}
   */
  public Observable<Hoard.Pair> retrieveAllReactive() {
    return RxReactiveStreams.toObservable(hoard.retrieveAllReactive());
  }
}
