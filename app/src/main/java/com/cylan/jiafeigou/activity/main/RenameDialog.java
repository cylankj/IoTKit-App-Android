package com.cylan.jiafeigou.activity.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.EditDelText;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2016-02-23
 * Time: 13:50
 */

public class RenameDialog extends Dialog {

    private Context mContext;
    private View confirm;
    private EditDelText edit;
    private String mOriginContent;

    public RenameDialog(final Context context, String content) {
        super(context, R.style.fun_dialog);
        this.mContext=context;
        this.mOriginContent=content;
        View view = View.inflate(context, R.layout.fg_modify_nickname, null);
        edit = (EditDelText) view.findViewById(R.id.nickname);
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        edit.setHint(R.string.EQUIPMENT_NAME);
        View dialogtitlebar = view.findViewById(R.id.rLayoutTitleBar);
        ((TextView) dialogtitlebar.findViewById(R.id.title)).setText(R.string.EQUIPMENT_NAME);
        edit.setText(content);
        confirm = dialogtitlebar.findViewById(R.id.right_btn);
        confirm.setVisibility(View.VISIBLE);
        ((TextView) confirm).setText(R.string.SAVE);
        view.findViewById(R.id.title_cover).getBackground().setAlpha(100);
        MyImageLoader.loadTitlebarImage(context, ((ImageView) dialogtitlebar.findViewById(R.id.title_background)));
        View cancel = dialogtitlebar.findViewById(R.id.ico_back);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                if (imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)) {
                    edit.clearFocus();
                    imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);

                }
                dismiss();
            }
        });
        setContentView(view);
        setCanceledOnTouchOutside(true);
    }

    public void setOnConfirmListener(final Request request) {
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edit.getText().toString().trim();
                if (name.isEmpty()) {
                    edit.setText(name);
                    ToastUtil.showFailToast(mContext, mContext.getString(R.string.DEViCE_NAME));
                } else if (name.equals(mOriginContent)) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
                    if (imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)) {
                        edit.clearFocus();
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);

                    }
                    dismiss();

                } else {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
                    if (imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)) {
                        edit.clearFocus();
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                    }

                    dismiss();

                    request.callback(name);
                }
            }
        });
    }

    public interface Request {
        void callback(String name);
    }

    public void setFullScreen(Activity activity){
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth(); //设置宽度
        getWindow().setAttributes(lp);
    }
}
