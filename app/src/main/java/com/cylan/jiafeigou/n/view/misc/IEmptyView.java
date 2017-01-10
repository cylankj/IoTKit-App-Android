package com.cylan.jiafeigou.n.view.misc;

import android.view.ViewGroup;

/**
 * Created by cylan-hunt on 16-8-2.
 */
public interface IEmptyView {
//    void loadView();

    void addView(ViewGroup viewGroup, ViewGroup.LayoutParams lp);

    /**
     * @param show： true to show ,false to hide
     */
    void show(boolean show);
}
