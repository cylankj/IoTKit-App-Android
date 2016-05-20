package com.cylan.jiafeigou.n.presenter;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-20.
 */
public abstract class AbstractPresenter implements BasePresenter {

    /**
     * @param task
     * @param workerThread :worker thread mode
     *                     {@link Schedulers#computation()}
     *                     {@link Schedulers#io()}
     *                     {@link Schedulers#immediate()}
     *                     {@link Schedulers#trampoline()}
     *                     {@link Schedulers#newThread()}
     */
    protected void submitCallbackUI(final Task task, Scheduler workerThread) {
        Observable.just(task)
                .subscribeOn(workerThread)
                .map(new Func1<Task, Object>() {
                    @Override
                    public Object call(Task task) {
                        return task.taskStart();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        task.taskFinish(o);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        task.taskFailed(throwable);
                    }
                });
    }

    /**
     * 指定工作线程,结果执行线程.
     *
     * @param task
     * @param workerThread
     * @param resultThread
     */
    protected void submit(final Task task, Scheduler workerThread, Scheduler resultThread) {
        Observable.just(task)
                .subscribeOn(workerThread)
                .map(new Func1<Task, Object>() {
                    @Override
                    public Object call(Task task) {
                        return task.taskStart();
                    }
                })
                .observeOn(resultThread)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        task.taskFinish(o);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        task.taskFailed(throwable);
                    }
                });
    }

}
