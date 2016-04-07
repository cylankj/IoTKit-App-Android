package cylan.scanbinding;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by hunt on 16-4-6.
 */
public class Worker {

    private HandlerThread handlerThread;

    private Handler handler;


    public Worker(String name) {
        handlerThread = new HandlerThread(name);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public Handler getHandler() {
        return handler;
    }
}
