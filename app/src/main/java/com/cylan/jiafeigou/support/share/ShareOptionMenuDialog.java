package com.cylan.jiafeigou.support.share;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.DialogShareOptionMenuBinding;


/**
 * Created by yanzhendong on 2017/6/1.
 */

public class ShareOptionMenuDialog extends DialogFragment {
    private DialogShareOptionMenuBinding shareBinding;
    private ShareOptionClickListener listener;
    private DialogInterface.OnCancelListener dismiss;

    public static ShareOptionMenuDialog newInstance(ShareOptionClickListener listener, boolean showTimeLine, boolean showWechatFriends, boolean showQQ, boolean showQZone, boolean showWeibo, boolean showTwitter, boolean showFacebook, boolean showLinks, DialogInterface.OnCancelListener dismiss) {
        ShareOptionMenuDialog dialog = new ShareOptionMenuDialog();
        Bundle argument = new Bundle();
        argument.putBoolean("showTimeLine", showTimeLine);
        argument.putBoolean("showWechatFriends", showWechatFriends);
        argument.putBoolean("showQQ", showQQ);
        argument.putBoolean("showQZone", showQZone);
        argument.putBoolean("showWeibo", showWeibo);
        argument.putBoolean("showTwitter", showTwitter);
        argument.putBoolean("showFacebook", showFacebook);
        argument.putBoolean("showLinks", showLinks);
        dialog.setArguments(argument);
        dialog.listener = listener;
        dialog.dismiss = dismiss;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shareBinding = DialogShareOptionMenuBinding.inflate(inflater);
        if (listener != null) {
            shareBinding.setOptionListener(listener);
        }
        Bundle arguments = getArguments();
        shareBinding.tvShareToTimeline.setVisibility(arguments.getBoolean("showTimeLine", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToWechatFriends.setVisibility(arguments.getBoolean("showWechatFriends", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToTencentQq.setVisibility(arguments.getBoolean("showQQ", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToTencentQzone.setVisibility(arguments.getBoolean("showQZone", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToSinaWeibo.setVisibility(arguments.getBoolean("showWeibo", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToTwitterFriends.setVisibility(arguments.getBoolean("showTwitter", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToFacebookFriends.setVisibility(arguments.getBoolean("showFacebook", true) ? View.VISIBLE : View.GONE);
        shareBinding.tvShareToByLinks.setVisibility(arguments.getBoolean("showLinks", true) ? View.VISIBLE : View.GONE);
        return shareBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (dismiss != null) {
            dismiss.onCancel(dialog);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (shareBinding != null) {
            shareBinding.setOptionListener(null);
            shareBinding.unbind();
        }
        listener = null;
    }


    public interface ShareOptionClickListener {
        void onShareOptionClick(int shareItemType);
    }
}
