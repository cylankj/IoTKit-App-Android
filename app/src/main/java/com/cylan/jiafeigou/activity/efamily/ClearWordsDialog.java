package com.cylan.jiafeigou.activity.efamily;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cylan.jiafeigou.R;


/**
 * Created by HeBin on 2015/3/2.
 */
public class ClearWordsDialog extends Dialog {
    Button clear;

    public ClearWordsDialog(Context context) {
        super(context, R.style.func_dialog);
        View content = View.inflate(context, R.layout.dialog_clearwords, null);
        TextView cancel = (TextView) content.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        clear = (Button) content.findViewById(R.id.clear);
        setContentView(content);
        setCanceledOnTouchOutside(true);

    }

    public void setClearListener(View.OnClickListener listener) {
        clear.setOnClickListener(listener);
    }

}
