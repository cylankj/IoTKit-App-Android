package com.cylan.jiafeigou.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.JniPlay;
import com.cylan.jiafeigou.base.MyApp;

public class NotifyDialog extends Dialog implements View.OnClickListener {

    protected TextView mTitleView;
    protected Button mPosBtn;
    protected Button mNegBtn;
    protected TextView mNotify;
    protected TextView mContent;
    private Context mContext;

    public NotifyDialog(Context context) {
        super(context, R.style.dialog);
        mContext = context;
        setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.dialog_notify, null);
        mTitleView = (TextView) view.findViewById(R.id.title);
        mPosBtn = (Button) view.findViewById(R.id.confirm);
        mNegBtn = (Button) view.findViewById(R.id.cancel);
        mNotify = (TextView) view.findViewById(R.id.notify);
        mContent = (TextView) view.findViewById(R.id.content);
        setContentView(view);
    }

    public void show(String nofity, final int error) {
        if (error == 22) {
            show(nofity, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                    MyApp.logout(mContext);
                    JniPlay.DisconnectFromServer();
                    MyApp.startActivityToSmartCall(mContext);
                }
            }, this);
        } else {
            show(nofity, this, this);
        }

    }

    public void show(int nofity) {
        show(getContext().getString(nofity), Constants.RETOK);
    }

    public void show(String nofity, View.OnClickListener listener1, View.OnClickListener listener2) {
        mNotify.setText(nofity);
        if (listener1 == null) {
            listener1 = this;
        }
        if (listener2 == null) {
            listener2 = this;
        }
        mPosBtn.setOnClickListener(listener1);
        mNegBtn.setOnClickListener(listener2);
        try {
            show();
        } catch (Exception e) {

        }
    }

    public void show(int msgID, String nofity, View.OnClickListener listener1, View.OnClickListener listener2) {
        mNotify.setText("("+ msgID +  ")" + nofity);
        if (listener1 == null) {
            listener1 = this;
        }
        if (listener2 == null) {
            listener2 = this;
        }
        mPosBtn.setOnClickListener(listener1);
        mNegBtn.setOnClickListener(listener2);
        try {
            show();
        } catch (Exception e) {

        }
    }

    public void show(int nofity, View.OnClickListener listener1, View.OnClickListener listener2) {
        show(getContext().getString(nofity), listener1, listener2);
    }


    public void setContent(String con) {
        mContent.setVisibility(View.VISIBLE);
        mContent.setText(con);
    }

    public void hidePosButton() {
        hideButton(mPosBtn);
    }

    public void hideNegButton() {
        mNegBtn.setVisibility(View.GONE);
        mPosBtn.setBackgroundResource(R.drawable.btn_dialog_middle_selector);
        hideButton(mNegBtn);
    }

    private void hideButton(View btn) {
        btn.setVisibility(View.GONE);
        View parent = (View) btn.getParent();
        LinearLayout.LayoutParams params = (LayoutParams) parent.getLayoutParams();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        parent.setLayoutParams(params);
    }

    public void setButtonText(int pos, int neg) {
        if (mPosBtn.getVisibility() == View.VISIBLE) {
            mPosBtn.setText(pos);
        }
        if (mNegBtn.getVisibility() == View.VISIBLE) {
            mNegBtn.setText(neg);
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public void setPosRedTheme(int res, int textColor) {
        // mPosBtn.setBackgroundResource(res);
        mPosBtn.setTextColor(textColor);
    }

    public void showUpdateTheme(String title) {
        mTitleView.setVisibility(View.VISIBLE);
        mTitleView.setText(title);
        mNotify.setVisibility(View.GONE);
    }


    public void setTitle(String title) {
        mTitleView.setText(title);
    }
}
