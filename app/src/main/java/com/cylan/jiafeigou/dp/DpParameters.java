package com.cylan.jiafeigou.dp;

import com.cylan.entity.jniCall.JFGDPMsg;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-11-8.
 */

public class DpParameters {

    public static class Builder {
        private ArrayList<JFGDPMsg> arrayList = new ArrayList<>();

        public Builder addParam(int msgId, long version) {
            JFGDPMsg msg = new JFGDPMsg();
            msg.id = msgId;
            msg.version = version;
            arrayList.add(msg);
            return this;
        }

        public ArrayList<JFGDPMsg> build() {
            return arrayList;
        }

        public Builder addAll(ArrayList<JFGDPMsg> all) {
            this.arrayList.addAll(all);
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "msgIdList=" + arrayList +
                    '}';
        }
    }
}
