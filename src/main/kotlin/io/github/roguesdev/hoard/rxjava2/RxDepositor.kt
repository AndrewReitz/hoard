package io.github.roguesdev.hoard.rxjava2

import io.github.roguesdev.hoard.ReactiveStreamDepositor
import io.reactivex.Completable
import io.reactivex.Single

/**
 * A [ReactiveStreamDepositor] that converts methods to RxJava 2.x
 * equivalents.
 *
 * @param <T> The type the depositor saves and retrieves.
 * @see io.github.roguesdev.hoard.Depositor
 *
 * @since 1.0
</T> */
interface RxDepositor<T> {
    fun store(value: T): Completable
    fun retrieve(): Single<T>
    fun delete(): Completable
    fun exists(): Single<Boolean>
}
