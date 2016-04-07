package cylan.scanbinding;

import android.net.wifi.ScanResult;

import cylan.scanbinding.view.BinderView;


/**
 * Created by hunt on 16-4-7.
 */
public interface Binder {

    void init(BinderView binderView);


    void start(Result scanResult);


    void onBinding(int state);

    void onFinish();

    class Result {
        public ScanResult scanResult;
        public String pwd;
    }
}
