package com.cylan.jiafeigou.n.mvp.model;

import android.content.pm.ResolveInfo;

/**
 * Created by hds on 17-6-8.
 */

public class ResolveInfoEx {
    private ResolveInfo info;

    public ResolveInfoEx setInfo(ResolveInfo info) {
        this.info = info;
        return this;
    }

    public ResolveInfo getInfo() {
        return info;
    }
}