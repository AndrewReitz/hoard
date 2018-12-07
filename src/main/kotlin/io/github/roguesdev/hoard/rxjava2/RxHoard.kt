package io.github.roguesdev.hoard.rxjava2

import io.github.roguesdev.hoard.Depositor
import io.github.roguesdev.hoard.Hoard
import io.reactivex.Completable
import io.reactivex.Observable

import java.lang.reflect.Type

@Suppress("NOTHING_TO_INLINE")
@JvmName("-toReactive")
inline fun Hoard.toReactive() = RxHoard(this)

/**
 * Wrapper for [Hoard] that produces [Depositor]s that can be used with RxJava 2.x
 *
 * @since 1.0
 */
class RxHoard(private val hoard: Hoard) {

    fun <T> createDepositor(key: String, typeToSave: Class<T>): RxDepositor<T> {
        return createDepositor(key, typeToSave as Type)
    }

    fun <T> createDepositor(key: String?, type: Type?): RxDepositor<T> {
        if (key == null) throw NullPointerException("key == null")
        if (type == null) throw NullPointerException("type == null")
        val reactiveDepositor = hoard.createReactiveDepositor<T>(key, type)
        return DefaultRxDepositor(reactiveDepositor)
    }

    /** Deletes all values stored by [Hoard].  */
    fun deleteAll() {
        hoard.deleteAll()
    }

    /**
     * Retrieve all values stored in [Hoard]. This will pull all values into memory and could
     * possibly cause problems. It is recommended to use [.retrieveAllRx] to avoid
     * heavy memory usage.
     * This can be useful for creating migrations.
     *
     * @return Map of all key-values stored.
     */
    fun retrieveAll(): Map<String, Any?> {
        return hoard.retrieveAll()
    }

    /**
     * Same as [.deleteAll] but wrapped in a [Completable].
     *
     * @return Completable that when subscribed will preform the delete operation.
     */
    fun deleteAllRx(): Completable {
        return Completable.fromPublisher(hoard.deleteAllReactive())
    }

    /**
     * Same as [.retrieveAll] but wrapped in a [Observable].
     *
     * @return Observable that when subscribed will retrieve the key value pairs stored in
     * [Hoard] and provide them as a [Pair]
     */
    fun retrieveAllRx(): Observable<Pair<String, Any?>> {
        return Observable.fromPublisher<Pair<String, Any?>>(hoard.retrieveAllReactive())
    }

    internal companion object {
        init {
            try {
                Class.forName("io.reactivex.Completable", false, RxHoard::class.java.classLoader)
            } catch (e: ClassNotFoundException) {
                throw IllegalStateException("RxJava 2.x is not found on the classpath. Please add " + "the RxJava 2.x library in order to use RxHoard2")
            }
        }
    }
}
