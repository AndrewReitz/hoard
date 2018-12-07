package io.github.roguesdev.hoard

import org.reactivestreams.Publisher
import org.reactivestreams.Subscription

internal class DefaultReactiveStreamDepositor<T>(private val depositor: Depositor<T>) : ReactiveStreamDepositor<T> {

  override fun store(value: T): Publisher<Void> {
    return Publisher { s ->
      s.onSubscribe(object : Subscription {

        @Volatile
        var canceled = false

        override fun request(n: Long) {
          try {
            depositor.store(value)

            if (canceled) return
            s.onComplete()
          } catch (e: Exception) {
            if (canceled) return
            s.onError(e)
          }
        }

        override fun cancel() {
          canceled = true
        }
      })
    }
  }

  override fun retrieve(): Publisher<T> {
    return Publisher { s ->
      s.onSubscribe(object : Subscription {

        @Volatile
        var canceled = false

        override fun request(n: Long) {
          try {
            val value = depositor.retrieve()
            if (canceled) return
            if (value != null) {
              s.onNext(value)
            }
            s.onComplete()
          } catch (e: Exception) {
            if (canceled) return
            s.onError(e)
          }
        }

        override fun cancel() {
          canceled = true
        }
      })
    }
  }

  override fun delete(): Publisher<Void> {
    return Publisher { s ->
      s.onSubscribe(object : Subscription {

        @Volatile var canceled = false

        override fun request(n: Long) {
          try {
            depositor.delete()
            if (canceled) return
            s.onComplete()
          } catch (e: Exception) {
            if (canceled) return
            s.onError(e)
          }
        }

        override fun cancel() {
          canceled = true
        }
      })
    }
  }

  override fun exists(): Publisher<Boolean> {
    return Publisher { s ->
      s.onSubscribe(object : Subscription {

        @Volatile var canceled = false

        override fun request(n: Long) {
          try {
            val exists = depositor.exists()

            if (canceled) return
            s.onNext(exists)
            s.onComplete()
          } catch (e: Exception) {
            if (canceled) return
            s.onError(e)
          }
        }

        override fun cancel() = Unit
      })
    }
  }
}
