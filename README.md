# XObservable
异步工具类：RXJava一样好用的链式调用却能覆盖RXJava某些无法覆盖的场景；更抽象的生命周期绑定；

1.仅仅支持子线程执行、主线程回调；

2.可绑定生命周期；

3.可指定异步任务线程；

4.可指定主线程任务Handler（XObservable默认会new新的handler）

5.和RXJava一样的链式调用，没什么学习成本；onNext也可多次回调；onComplete会自动调用；

6.抽象的生命周期IExtraLife：实现IExtraLife接口重写isLifeDestroy接口给出当前任务不再执行的条件即可（比如需要绑定观察者或者频繁刷新的可复用控件通过tag判断是否回调刷新控件）

# 用这个类的原因

1.Rxjava在使用默认异步线程池Schedulers.io()/Schedulers.computation()会无限制生成线程，极端场合线程紧张引起crash。

2.Rxjava指定异步线程池在极端场合也会抛出error（忘了叫啥名了），即便设置了全局ErrorHandler（RxJavaPlugins.setErrorHandler）也不行，据说新版本RXjava（2.2）会解决但测试后没解决

3.生命周期绑定不友好……


###用法
```XObservable.create(new XObservable.ObservableOnSubscribe<Integer>() {
                @Override
                public void subscribe(Emitter<Integer> emitter) {
                // todo 子线程执行异步任务，结果通过onNext返回给主线程，可执行多次onNext；
                    emitter.onNext(0);
                    emitter.onNext(1);
                }
            }).bindLifeCycle(MainActivity.this.getLifecycle()) // 绑定生命周期，在ondestroy时不会回调回主线程
                    .bindExtraLife(this, mScoreView.getTag()) // 抽象的生命周期，可理解为是否执行后续操作的判断条件， 
//                        .setMainHandler(new Handler()) //指定抛回主线程的handler， 不指定会new一个
                    .executeOnExecutor(XThreadPoolManager.getThreadPool()) // 指定异步任务的线程， 不指定则使用默认线程池
                    .subscribe(new XObservable.Consumer<Integer>() {
                        @Override
                        public void accept(Integer result) {
                        // todo主线程回调
                        }
                    });
```
