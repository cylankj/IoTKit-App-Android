package com.cylan.jiafeigou.engine;

import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.listener.RequestCallback;

import java.util.ArrayList;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-11-25
 * Time: 09:12
 */

public class CallbackManager {

    private ArrayList<RequestCallback> mObservers = new ArrayList<>();

    private CallbackManager() {
    }

    public static CallbackManager getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static CallbackManager instance = new CallbackManager();
    }

    public void addObserver(RequestCallback observer) {
        if (observer != null) {
            mObservers.add(observer);
        }
    }

    public void delObserver(RequestCallback observer) {
        mObservers.remove(observer);
    }

    public void clearObserver() {
        mObservers.clear();
    }

    public void notifyObservers(int msgId, Object msg) {
        try {
            for (int i = 0, size = mObservers.size(); i < size; i++) {
                mObservers.get(i).handleMsg(msgId, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.ex(e.toString());
        }
    }
}
