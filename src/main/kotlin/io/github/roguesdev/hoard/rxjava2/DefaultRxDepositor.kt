package io.github.roguesdev.hoard.rxjava2

import io.github.roguesdev.hoard.ReactiveStreamDepositor
import io.reactivex.Completable
import io.reactivex.Single

internal class DefaultRxDepositor<T>(private val depositor: ReactiveStreamDepositor<T>) : RxDepositor<T> {

    override fun store(value: T): Completable {
        return Completable.fromPublisher(depositor.store(value))
    }

    override fun retrieve(): Single<T> {
        return Single.fromPublisher(depositor.retrieve())
    }

    override fun delete(): Completable {
        return Completable.fromPublisher(depositor.delete())
    }

    override fun exists(): Single<Boolean> {
        return Single.fromPublisher(depositor.exists())
    }
}
