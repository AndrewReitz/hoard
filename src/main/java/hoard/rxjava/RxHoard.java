package hoard.rxjava;

import hoard.Depositor;
import hoard.Hoard;
import hoard.ReactiveStreamDepositor;
import java.lang.reflect.Type;

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
}
