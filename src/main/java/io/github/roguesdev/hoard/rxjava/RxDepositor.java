package io.github.roguesdev.hoard.rxjava;

import io.github.roguesdev.hoard.ReactiveStreamDepositor;
import rx.Observable;

/**
 * A {@link ReactiveStreamDepositor} that converts methods to RxJava 1.x
 * equivalents. This utilizes RxReactiveStreams in order to do so.
 *
 * This uses {@link Observable} rather than Single and Completable in order to be utilized with
 * RxJava 1.0.0 and above.
 *
 * @param <T> The type the depositor saves and retrieves.
 * @since 1.0
 */
public interface RxDepositor<T> {
  Observable<Void> store(T value);
  Observable<T> retrieve();
  Observable<Void> delete();
}
