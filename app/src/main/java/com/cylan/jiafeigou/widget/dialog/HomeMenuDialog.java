package com.cylan.jiafeigou.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;

/**
 * Created by hunt on 16-5-25.
 */

/**
 * 主页长按设备弹框
 */
public class HomeMenuDialog extends Dialog {


    public HomeMenuDialog(Context context,
                          final View.OnClickListener cancelListener,
                          final View.OnClickListener confirmListener) {
        super(context, R.style.dialog);
        setCanceledOnTouchOutside(false);
        View view = View.inflate(context, R.layout.dialog_home_menu, null);
        view.findViewById(R.id.tv_dialog_home_menu_cancel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (cancelListener != null)
                            cancelListener.onClick(v);
                    }
                });
        view.findViewById(R.id.tv_dialog_home_menu_delete)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (confirmListener != null)
                            confirmListener.onClick(v);
                    }
                });
        setContentView(view);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = display.getWidth(); // set width
        this.getWindow().setAttributes(lp);
    }

}
