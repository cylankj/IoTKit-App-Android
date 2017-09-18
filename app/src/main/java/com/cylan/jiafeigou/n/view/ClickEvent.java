package com.cylan.jiafeigou.n.view;

import android.view.View;

/**
 * Created by hds on 17-9-18.
 */

public abstract class ClickEvent {

    private final int pid;

    public ClickEvent(int pid) {
        this.pid = pid;
    }

    public void preClick() {
    }

    public void afterClick() {
    }


    public static class HotspotJumper extends ClickEvent implements View.OnClickListener {

        public HotspotJumper(int pid) {
            super(pid);
        }


        @Override
        public void onClick(View v) {
            preClick();

            afterClick();
        }
    }
}
