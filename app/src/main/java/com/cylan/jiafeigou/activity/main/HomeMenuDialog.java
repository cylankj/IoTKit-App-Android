package com.cylan.jiafeigou.activity.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

public class HomeMenuDialog extends Dialog {

    private TextView mSetView;
    private TextView mDelView;
    private Context mContext;

    public HomeMenuDialog(Context context) {
        super(context, R.style.dialog);
        this.mContext = context;
        setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.dialog_videomenu, null);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

            }

        });
        mSetView = (TextView) view.findViewById(R.id.setting);
        mDelView = (TextView) view.findViewById(R.id.delete);
        setContentView(view);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = display.getWidth(); // set width
        this.getWindow().setAttributes(lp);
    }

    public void setListenter(android.view.View.OnClickListener a, android.view.View.OnClickListener b) {
        mSetView.setOnClickListener(a);
        mDelView.setOnClickListener(b);
    }

    public void hideSettingBtn() {
        mSetView.setVisibility(View.GONE);
        mDelView.setBackgroundResource(R.drawable.bg_single_longclick_item_selector);
    }
}