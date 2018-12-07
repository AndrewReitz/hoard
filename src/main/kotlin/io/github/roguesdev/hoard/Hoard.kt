package io.github.roguesdev.hoard

import io.github.roguesdev.hoard.serialization.Serializer
import java.io.File
import java.lang.reflect.Type
import java.util.LinkedHashMap
import org.reactivestreams.Publisher
import org.reactivestreams.Subscription

/**
 * Factory for creating new [Depositor] or [ReactiveStreamDepositor]. Also provides
 * utilities for doing large operations for clearing all values, and retrieving all values.
 *
 * @since 1.0
 */
class Hoard(
  private val rootDirectory: File,
  private val serializer: Serializer
) {

  init {
    if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
      throw IllegalArgumentException("rootDirectory does not exist, and can not be created")
    }
    if (!rootDirectory.isDirectory) {
      throw IllegalArgumentException("rootDirectory is not a directory")
    }
  }

  fun <T> createDepositor(key: String, typeToSave: Class<T>): Depositor<T> {
    return createDepositor(key, typeToSave as Type)
  }

  fun <T> createDepositor(key: String, type: Type): Depositor<T> {
    return DefaultDepositor(serializer, rootDirectory, key, type)
  }

  inline fun <reified T> createDepositor(key: String): Depositor<T> {
    return createDepositor(key, T::class.java)
  }

  fun <T> createReactiveDepositor(key: String, type: Class<T>): ReactiveStreamDepositor<T> {
    return createReactiveDepositor(key, type as Type)
  }

  fun <T> createReactiveDepositor(key: String, type: Type): ReactiveStreamDepositor<T> {
    val depositor = createDepositor<T>(key, type)
    return DefaultReactiveStreamDepositor(depositor)
  }

  inline fun <reified T> createReactiveDepositor(key: String): ReactiveStreamDepositor<T> {
    return createReactiveDepositor(key, T::class.java)
  }

  /** Deletes all values stored by [Hoard].  */
  fun deleteAll() {
    val files = rootDirectory.listFiles() ?: return
    for (file in files) {
      file.delete()
    }
  }

  /**
   * Retrieve all values stored in [Hoard]. This will pull all values into memory and could
   * possibly cause problems. It is recommended to use [.retrieveAllReactive] to avoid
   * heavy memory usage.
   * This can be useful for creating migrations.
   *
   * @return Map of all key-values stored.
   */
  fun retrieveAll(): Map<String, Any?> {
    val returnValues = LinkedHashMap<String, Any?>()
    val list = rootDirectory.list() ?: return returnValues

    for (name in list) {
      val retrieve = createDepositor(name, Any::class.java).retrieve()
      returnValues[name] = retrieve
    }

    return returnValues
  }

  /**
   * Same as [.deleteAll] but wrapped in a [Publisher].
   *
   * @return Publisher that when subscribed will preform the delete operation.
   */
  fun deleteAllReactive(): Publisher<Unit> {
    return Publisher { s ->
      s.onSubscribe(object : Subscription {
        @Volatile var canceled = false

        override fun request(n: Long) {
          try {
            val files = rootDirectory.listFiles() ?: return
            for (file in files) {
              file.delete()
            }
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

  /**
   * Same as [.retrieveAll] but wrapped in a [Publisher].
   *
   * @return Publisher that when subscribed will retrieve the key value pairs stored in
   * [Hoard] and provide them as a [Pair]
   */
  fun retrieveAllReactive(): Publisher<Pair<String, Any?>> {
    return Publisher { s ->
      s.onSubscribe(object : Subscription {

        @Volatile var canceled = false

        override fun request(n: Long) {
          val list = rootDirectory.list()
          if (list == null) {
            if (canceled) return
            s.onComplete()
            return
          }

          for (name in list) {
            val retrieve = createDepositor(name, Any::class.java).retrieve()
            val value = Pair(name, retrieve)
            if (canceled) return
            s.onNext(value)
          }

          if (canceled) return
          s.onComplete()
        }

        override fun cancel() {
          canceled = true
        }
      })
    }
  }
}
