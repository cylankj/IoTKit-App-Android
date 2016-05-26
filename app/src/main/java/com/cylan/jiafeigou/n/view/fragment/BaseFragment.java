package com.cylan.jiafeigou.n.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by hunt on 16-5-26.
 */
//带有title bar 的fragment
public abstract class BaseFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_new_base_fragment, null);
        addSubContentView((ViewGroup) view.findViewById(R.id.rLayout_base_container));
//        tvBack = (TextView) view.findViewById(R.id.tv_base_title_back);
//        tvTitle = (TextView) view.findViewById(R.id.tv_base_title_title);
//        tvDone = (TextView) view.findViewById(R.id.tv_base_title_done);
        return view;
    }

    protected abstract void addSubContentView(ViewGroup viewGroup);

    protected void setContent(final int viewId, CharSequence charSequence) {
        TextView tv = getView() != null ? (TextView) getView().findViewById(viewId) : null;
        if (tv == null)
            return;
        tv.setText(charSequence);
    }

    protected void setContent(final int viewId, @StringRes int titleRes) {
        setContent(viewId, getString(titleRes));
    }

    protected void setListener(final int viewId, View.OnClickListener clickListener) {
        View view = getView() != null ? getView().findViewById(viewId) : null;
        if (view == null)
            return;
        view.setOnClickListener(clickListener);
    }

    public static class TitleActionInfo {
        public View.OnClickListener backListener;
        public View.OnClickListener doneListener;
        public View.OnClickListener titleListener;
        public String titleContent;
        public String backContent;
        public String doneContent;
    }
}
