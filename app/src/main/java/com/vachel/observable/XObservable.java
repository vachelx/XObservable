package com.vachel.observable;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

/**
 * 仿Rxjava基于Runnable写的异步任务处理类（仅在子线程执行然后抛回主线程间回调）
 * mLifecycle 通用的生命周期，onDestroy时会清除任务（线程池队列中还没执行的任务，已执行的不会打断），并且不会回调到主线程；
 * IExtraLife 抽象的生命周期， 可理解为是否回调回主线程的判断条件；
 *
 */
public class XObservable<T> implements LifecycleObserver, Emitter<T> {
    private ObservableOnSubscribe<T> mSubscribe;
    private boolean isInterrupt;
    private Object mTag;
    private WeakReference<IExtraLife> mExtraRef;
    private Lifecycle mLifecycle;
    private Consumer<T> mConsumer;
    private Handler mHandler;
    private ExecutorService mExecutorService;

    private XObservable() {

    }

    public static <T> XObservable<T> create(ObservableOnSubscribe<T> subscribe) {
        XObservable<T> XObservable = new XObservable<T>();
        XObservable.mSubscribe = subscribe;
        return XObservable;
    }

    public XObservable<T> bindLifeCycle(Lifecycle lifecycle) {
        mLifecycle = lifecycle;
        return this;
    }

    /**
     * 较为抽象的自定义生命周期，需要实现IExtraLife，重写isLifeDestroy（）方法用以确认生命状态结束标志
     */
    public XObservable<T> bindExtraLife(IExtraLife extra, Object tag) {
        mExtraRef = new WeakReference<>(extra);
        mTag = tag;
        return this;
    }

    public XObservable<T> executeOnExecutor(ExecutorService exec) {
        mExecutorService = exec;
        return this;
    }

    public XObservable<T> setMainHandler(Handler handler) {
        mHandler = handler;
        return this;
    }

    public XObservable<T> subscribe(@NonNull final Consumer<T> consumer) {
        if (mLifecycle != null) {
            mLifecycle.addObserver(this);
        }
        mConsumer = consumer;
        run();
        return this;
    }

    private void run() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isInterrupt()) {
                    onComplete();
                    return;
                }
                if (mSubscribe != null) {
                    mSubscribe.subscribe(XObservable.this);
                }
                onComplete();
            }
        };
        if (mExecutorService == null) {
            XThreadPoolManager.getThreadPool().execute(runnable);
        } else {
            mExecutorService.execute(runnable);
        }
    }


    @Override
    public void onNext(final T result) {
        if (isInterrupt()) {
            return;
        }
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isInterrupt() || mConsumer == null) {
                    return;
                }
                mConsumer.accept(result);
            }
        });
    }

    @Override
    public void onComplete() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                onDestroy();
            }
        });
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        interrupt();
    }

    private void unregisterLifeCycle() {
        if (mLifecycle != null) {
            mLifecycle.removeObserver(this);
            mLifecycle = null;
        }
    }

    public void interrupt() {
        isInterrupt = true;
        mSubscribe = null;
        mConsumer = null;
        unregisterLifeCycle();
    }

    public boolean isInterrupt() {
        if (isInterrupt) {
            return true;
        }
        if (mExtraRef != null && mExtraRef.get() != null) {
            return mExtraRef.get().isLifeDestroy(mTag);
        }
        return false;
    }

    public interface ObservableOnSubscribe<T> {
        void subscribe(Emitter<T> emitter);
    }

    public interface Consumer<T> {
        void accept(T result);
    }

    public interface IExtraLife {
        boolean isLifeDestroy(Object tag);
    }
}
