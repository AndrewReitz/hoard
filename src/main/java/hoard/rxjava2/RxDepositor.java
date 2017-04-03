package hoard.rxjava2;

import hoard.ReactiveStreamDepositor;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * A {@link ReactiveStreamDepositor} that converts methods to RxJava 2.x
 * equivalents.
 *
 * @param <T> The type the depositor saves and retrieves.
 * @since 1.0
 */
public interface RxDepositor<T> {
  Completable store(T value);
  Single<T> retrieve();
  Completable delete();
}
