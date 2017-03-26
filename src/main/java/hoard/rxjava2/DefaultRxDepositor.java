package hoard.rxjava2;

import hoard.ReactiveStreamDepositor;
import io.reactivex.Completable;
import io.reactivex.Single;

class DefaultRxDepositor<T> implements RxDepositor<T> {

  private final ReactiveStreamDepositor<T> depositor;

  DefaultRxDepositor(ReactiveStreamDepositor<T> depositor) {
    this.depositor = depositor;
  }

  @Override public Completable save(T value) {
    return Completable.fromPublisher(depositor.save(value));
  }

  @Override public Single<T> retrieve() {
    return Single.fromPublisher(depositor.retrieve());
  }

  @Override public Completable delete() {
    return Completable.fromPublisher(depositor.delete());
  }
}
