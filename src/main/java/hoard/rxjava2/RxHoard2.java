package hoard.rxjava2;

import hoard.Depositor;
import hoard.Hoard;
import hoard.ReactiveStreamDepositor;
import java.lang.reflect.Type;

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
}
