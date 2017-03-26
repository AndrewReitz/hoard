package hoard;

import org.reactivestreams.Publisher;

/**
 * A {@link Depositor} that uses {@link Publisher} from reactive streams in order to make
 * Hoard comparable with comparable Reactive Libraries such as RxJava.
 *
 * @param <T> The type the depositor saves and retrieves.
 * @since 1.0
 */
public interface ReactiveStreamDepositor<T> {
  Publisher<Void> save(T value);
  Publisher<T> retrieve();
  Publisher<Void> delete();
}
