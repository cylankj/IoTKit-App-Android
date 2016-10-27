package com.cylan.jiafeigou.n.view.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-7-26.
 */
public class ShareDialogFragment extends BaseDialog {

    @BindView(R.id.lLayout_dialog_share_wonderful)
    CardView lLayoutDialogShareWonderful;
    @BindView(R.id.tv_share_to_timeline)
    TextView tvShareToFriends;
    @BindView(R.id.tv_share_to_wechat_friends)
    TextView tvShareToWechat;

    public static ShareDialogFragment newInstance(Bundle bundle) {
        ShareDialogFragment fragment = new ShareDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_dialog_share_wonderful, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.tv_share_to_timeline, R.id.tv_share_to_wechat_friends})
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.tv_share_to_timeline:
                if (shareToListener != null)
                    shareToListener.share(R.id.tv_share_to_timeline);
                break;
            case R.id.tv_share_to_wechat_friends:
                if (shareToListener != null)
                    shareToListener.share(R.id.tv_share_to_wechat_friends);
                break;
        }
    }

    public void setShareToListener(ShareToListener shareToListener) {
        this.shareToListener = shareToListener;
    }

    ShareToListener shareToListener;

    public interface ShareToListener {
        void share(int id);
    }
}
