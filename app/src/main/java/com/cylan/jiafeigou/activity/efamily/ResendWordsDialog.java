package com.cylan.jiafeigou.activity.efamily;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

public class ResendWordsDialog extends Dialog {
    Button resend;

    public ResendWordsDialog(Context context) {
        super(context, R.style.func_dialog);
        View content = View.inflate(context, R.layout.dialog_resend_words, null);
        TextView cancel = (TextView) content.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        resend = (Button) content.findViewById(R.id.resend);
        setContentView(content);
        setCanceledOnTouchOutside(true);
    }

    public void setResendListener(View.OnClickListener listener) {
        resend.setOnClickListener(listener);
    }

}
