package com.vachel.observable;

public interface Emitter<T> {
    void onNext(T result);

    void onComplete();
}
