package android.arch.rxlifecycle

import com.trello.rxlifecycle2.LifecycleProvider
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by 张宇 on 2018/3/19.
 * E-mail: zhangyu4@yy.com
 * YY: 909017428
 */
abstract class LiveData<DATA> : Observable<DATA>() {

    private val cnt = Counter()

    override fun subscribeActual(observer: Observer<in DATA>) {
        mObservers.subscribe(LiveDataObservable(observer))
    }

    private val mObservers = BehaviorSubject.create<DATA>()

    protected open fun setValue(value: DATA) = mObservers.onNext(value)

    fun getValue(): DATA? = mObservers.value

    fun hasObservers(): Boolean = mObservers.hasObservers()

    fun hasActiveObservers(): Boolean = cnt.i > 0

    fun <EVENT> bindLifecycle(lifecycle: LifecycleProvider<EVENT>): Observable<DATA> {
        return compose(lifecycle.bindToLifecycle())
    }

    open fun onActive() {}

    open fun onInactive() {}

    private inner class LiveDataObservable(
            val actual: Observer<in DATA>
    ) : Observer<DATA> {

        override fun onComplete() = actual.onComplete()

        override fun onSubscribe(d: Disposable) {
            actual.onSubscribe(LiveDataDisposable(d))
        }

        override fun onNext(t: DATA) = actual.onNext(t)

        override fun onError(e: Throwable) = actual.onError(e)
    }

    private inner class LiveDataDisposable(
            val actual: Disposable
    ) : Disposable, AtomicBoolean() {

        init {
            cnt.increase()
        }

        override fun isDisposed(): Boolean = get()

        override fun dispose() {
            if (compareAndSet(false, true)) {
                actual.dispose()
                cnt.decrease()
            }
        }
    }

    private inner class Counter {

        @Volatile
        var i = 0

        fun increase() {
            synchronized(this) {
                val wasInactive = i == 0
                i++
                if (wasInactive) {
                    onActive()
                }
            }
        }

        fun decrease() {
            synchronized(this) {
                i--
                if (i == 0) {
                    onInactive()
                }
            }
        }
    }
}