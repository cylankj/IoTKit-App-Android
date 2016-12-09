package com.cylan.jiafeigou.widget.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-6.
 */

public class VideoMoreDialog extends BaseDialog {
    public static VideoMoreDialog newInstance(Bundle bundle) {
        return new VideoMoreDialog();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_video_more, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.dialog_media_video_delete, R.id.dialog_media_video_download, R.id.dialog_media_video_share})
    public void onClick(View view) {
        if (action != null) action.onDialogAction(view.getId(), view);
    }
}

