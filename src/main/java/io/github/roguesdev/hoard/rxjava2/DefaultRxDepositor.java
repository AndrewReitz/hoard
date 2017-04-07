package io.github.roguesdev.hoard.rxjava2;

import io.github.roguesdev.hoard.ReactiveStreamDepositor;
import io.reactivex.Completable;
import io.reactivex.Single;

class DefaultRxDepositor<T> implements RxDepositor<T> {

  private final ReactiveStreamDepositor<T> depositor;

  DefaultRxDepositor(ReactiveStreamDepositor<T> depositor) {
    this.depositor = depositor;
  }

  @Override public Completable store(T value) {
    return Completable.fromPublisher(depositor.store(value));
  }

  @Override public Single<T> retrieve() {
    return Single.fromPublisher(depositor.retrieve());
  }

  @Override public Completable delete() {
    return Completable.fromPublisher(depositor.delete());
  }

  @Override public Single<Boolean> exists() {
    return Single.fromPublisher(depositor.exists());
  }
}
