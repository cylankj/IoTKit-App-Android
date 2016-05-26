#### 基于RxJava,封装`EventBus`的说明.


*   RxJava本身算是一种流式的操作.
>   订阅-->接收通知--反订阅
>   在某些类订阅了一个对象`xxxClass`,在消息中心,派送这个消息的对象.



```
        //消息通知
        if (_rxBus.hasObservers()) {
             _rxBus.send(new RxBusDemoFragment.TapEvent());
        }
```




```java

        //注册1
        _subscriptions = new CompositeSubscription();
        _subscriptions//
                .add(_rxBus.toObservable()
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object event) {
                                if (event instanceof RxBusDemoFragment.TapEvent) {
                                    _showTapText();
                                }
                            }
                        }));
```


```java
        //注册2
_subscriptions = new CompositeSubscription();
        ConnectableObservable<Object> tapEventEmitter = _rxBus.toObservable().publish();

        _subscriptions
                .add(tapEventEmitter.subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof RxBusDemoFragment.TapEvent) {
                            _showTapText();
                        }
                    }
                }));

        _subscriptions
                .add(tapEventEmitter.publish(new Func1<Observable<Object>, Observable<List<Object>>>() {
                    @Override
                    public Observable<List<Object>> call(Observable<Object> stream) {
                        return stream.buffer(stream.debounce(1, TimeUnit.SECONDS));
                    }
                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> taps) {
                        _showTapCount(taps.size());
                    }
                }));

        _subscriptions.add(tapEventEmitter.connect());
```


```
//注册3
        Observable<Object> tapEventEmitter = _rxBus.toObservable().share();
        Observable<Object> debouncedEmitter = tapEventEmitter.debounce(1, TimeUnit.SECONDS);
        Observable<List<Object>> debouncedBufferEmitter = tapEventEmitter.buffer(debouncedEmitter);
        _subscriptions
                .add(debouncedBufferEmitter
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<Object>>() {
                            @Override
                            public void call(List<Object> taps) {
                                _showTapCount(taps.size());
                            }
                        }));
```