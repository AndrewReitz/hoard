package io.github.roguesdev.hoard

import org.reactivestreams.Publisher

/**
 * A [Depositor] that uses [Publisher] from reactive streams in order to make
 * Hoard comparable with comparable Reactive Libraries such as RxJava.
 * All operations should be considered blocking IO operations.
 *
 * @param T The type the depositor saves and retrieves.
 * @since 1.0
 */
interface ReactiveStreamDepositor<T> {
    fun store(value: T): Publisher<Void>
    fun retrieve(): Publisher<T>
    fun delete(): Publisher<Void>
    fun exists(): Publisher<Boolean>
}
