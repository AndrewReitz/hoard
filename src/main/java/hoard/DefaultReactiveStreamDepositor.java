package hoard;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

class DefaultReactiveStreamDepositor<T> implements ReactiveStreamDepositor<T> {

  private final Depositor<T> depositor;

  DefaultReactiveStreamDepositor(Depositor<T> depositor) {
    this.depositor = depositor;
  }

  @Override public Publisher<Void> save(final T value) {
    return new Publisher<Void>() {
      @Override public void subscribe(Subscriber<? super Void> s) {
        try {
          depositor.save(value);
          s.onComplete();
        } catch (Exception e) {
          s.onError(e);
        }
      }
    };
  }

  @Override public Publisher<T> retrieve() {
    return new Publisher<T>() {
      @Override public void subscribe(Subscriber<? super T> s) {
        try {
          T value = depositor.retrieve();
          s.onNext(value);
          s.onComplete();
        } catch (Exception e) {
          s.onError(e);
        }
      }
    };
  }

  @Override public Publisher<Void> delete() {
    return new Publisher<Void>() {
      @Override public void subscribe(Subscriber<? super Void> s) {
        try {
          depositor.delete();
          s.onComplete();
        } catch (Exception e) {
          s.onError(e);
        }
      }
    };
  }
}
