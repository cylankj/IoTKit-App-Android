package com.cylan.jiafeigou.n.presenter;

import android.util.Log;
import android.widget.Toast;

import com.cylan.jiafeigou.n.view.BaseView;

import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-20.
 */
public class TestPresenter extends AbstractPresenter {


    BaseView baseView;

    public TestPresenter(BaseView baseView) {
        this.baseView = baseView;
    }

    private static final String TAG = "TestPresenter";

    @Override
    public void start() {
        Toast.makeText(baseView.getContext(), "start: ", Toast.LENGTH_LONG).show();
        Task<String> task = new TestTask();
        submitCallbackUI(task, Schedulers.newThread());
    }

    private class TestTask implements Task<String> {

        @Override
        public String taskStart() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "ahdhdh");
            return "ni hao";
        }

        @Override
        public void taskFinish(String task) {
            Toast.makeText(baseView.getContext(), "result: " + task, Toast.LENGTH_LONG).show();
        }

        @Override
        public void taskFailed(Throwable throwable) {
            Log.d(TAG, ": " + throwable.toString());
        }
    }
}
