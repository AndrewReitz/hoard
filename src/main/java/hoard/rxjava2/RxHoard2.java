package hoard.rxjava2;

import hoard.Depositor;
import hoard.Hoard;
import hoard.ReactiveStreamDepositor;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Wrapper for {@link Hoard} that produces {@link Depositor}s that can be used with RxJava 2.x
 *
 * @since 1.0
 */
public class RxHoard2 {
  static {
    try {
      Class.forName("io.reactivex.Completable", false, RxHoard2.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("RxJava 2.x is not found on the classpath. Please add "
        + "the RxJava 2.x library in order to use RxHoard2");
    }
  }

  private final Hoard hoard;

  public RxHoard2(Hoard hoard) {
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
   * possibly cause problems. It is recommended to use {@link #rxRetrieveAll()} to avoid
   * heavy memory usage.
   * This can be useful for creating migrations.
   *
   * @return Map of all key-values stored.
   */
  public Map<String, Object> retrieveAll() {
    return hoard.retrieveAll();
  }

  /**
   * Same as {@link #deleteAll()} but wrapped in a {@link Completable}.
   *
   * @return Completable that when subscribed will preform the delete operation.
   */
  public Completable rxDeleteAll() {
    return Completable.fromPublisher(hoard.deleteAllReactive());
  }

  /**
   * Same as {@link #retrieveAll()} but wrapped in a {@link Observable}.
   *
   * @return Observable that when subscribed will retrieve the key value pairs stored in
   * {@link Hoard} and provide them as a {@link Hoard.Pair}
   */
  public Observable<Hoard.Pair> rxRetrieveAll() {
    return Observable.fromPublisher(hoard.retrieveAllReactive());
  }
}
