package io.github.roguesdev.hoard.rxjava;

import io.github.roguesdev.hoard.ReactiveStreamDepositor;
import rx.Observable;
import rx.RxReactiveStreams;

class DefaultRxDepositor<T> implements RxDepositor<T> {

  private final ReactiveStreamDepositor<T> depositor;

  DefaultRxDepositor(ReactiveStreamDepositor<T> depositor) {
    this.depositor = depositor;
  }

  @Override public Observable<Void> store(T value) {
    return RxReactiveStreams.toObservable(depositor.store(value));
  }

  @Override public Observable<T> retrieve() {
    return RxReactiveStreams.toObservable(depositor.retrieve());
  }

  @Override public Observable<Void> delete() {
    return RxReactiveStreams.toObservable(depositor.delete());
  }
}
