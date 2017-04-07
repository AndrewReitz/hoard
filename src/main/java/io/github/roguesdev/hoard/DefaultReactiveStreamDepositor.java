package io.github.roguesdev.hoard;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

class DefaultReactiveStreamDepositor<T> implements ReactiveStreamDepositor<T> {

  private final Depositor<T> depositor;

  DefaultReactiveStreamDepositor(Depositor<T> depositor) {
    this.depositor = depositor;
  }

  @Override public Publisher<Void> store(final T value) {
    return new Publisher<Void>() {
      @Override public void subscribe(final Subscriber<? super Void> s) {
        s.onSubscribe(new Subscription() {

          volatile boolean canceled = false;

          @Override public void request(long n) {
            try {
              depositor.store(value);

              if (canceled) return;
              s.onComplete();
            } catch (Exception e) {
              if (canceled) return;
              s.onError(e);
            }
          }

          @Override public void cancel() {
            canceled = true;
          }
        });
      }
    };
  }

  @Override public Publisher<T> retrieve() {
    return new Publisher<T>() {
      @Override public void subscribe(final Subscriber<? super T> s) {
        s.onSubscribe(new Subscription() {

          volatile boolean canceled = false;

          @Override public void request(long n) {
            try {
              T value = depositor.retrieve();
              if (canceled) return;
              if (value != null) {
                s.onNext(value);
              }
              s.onComplete();
            } catch (Exception e) {
              if (canceled) return;
              s.onError(e);
            }
          }

          @Override public void cancel() {
            canceled = true;
          }
        });
      }
    };
  }

  @Override public Publisher<Void> delete() {
    return new Publisher<Void>() {
      @Override public void subscribe(final Subscriber<? super Void> s) {
        s.onSubscribe(new Subscription() {

          volatile boolean canceled = false;

          @Override public void request(long n) {
            try {
              depositor.delete();
              if (canceled) return;
              s.onComplete();
            } catch (Exception e) {
              if (canceled) return;
              s.onError(e);
            }
          }

          @Override public void cancel() {
            canceled = true;
          }
        });
      }
    };
  }

  @Override public Publisher<Boolean> exists() {
    return new Publisher<Boolean>() {
      @Override public void subscribe(final Subscriber<? super Boolean> s) {
        s.onSubscribe(new Subscription() {

          volatile boolean canceled = false;

          @Override public void request(long n) {
            try {
              boolean exists = depositor.exists();

              if (canceled) return;
              s.onNext(exists);
              s.onComplete();
            } catch (Exception e) {
              if (canceled) return;
              s.onError(e);
            }
          }

          @Override public void cancel() {

          }
        });
      }
    };
  }
}
