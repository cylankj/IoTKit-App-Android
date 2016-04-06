package com.cylan.jiafeigou.activity.message;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

public class MessageDelDialog extends Dialog {

    private TextView mDelView;

    public MessageDelDialog(Context context) {
        super(context, R.style.dialog);
        setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.dialog_delete_msg, null);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

            }

        });
        mDelView = (TextView) view.findViewById(R.id.delete);
        setContentView(view);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = display.getWidth(); // set width
        this.getWindow().setAttributes(lp);
    }

    public void setListenter(View.OnClickListener a) {
        mDelView.setOnClickListener(a);
    }


}