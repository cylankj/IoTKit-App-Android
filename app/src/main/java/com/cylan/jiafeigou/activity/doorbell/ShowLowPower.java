package com.cylan.jiafeigou.activity.doorbell;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.main.MyAccount;

/**
 * Created by yangc on 2015/9/25.
 *
 */
public class ShowLowPower extends Dialog implements View.OnClickListener{

    Button confirm;
    Window window;
    ImageView title;
    TextView tv1;
    TextView tv2;

    public static final int SHOWLOWPOWER = 0;
    public static final int SHOWSETIMAGE = 1;
    private int flag = 0;
    private Context mContext;

    public ShowLowPower(Context context, int state) {
        super(context, R.style.dialog);
        mContext = context;
        View view = View.inflate(context, R.layout.dialog_low_power,null);
        confirm = (Button) view.findViewById(R.id.low_power_confirm);
        tv1 = (TextView) view.findViewById(R.id.dialog_text1);
        tv2 = (TextView) view.findViewById(R.id.dialog_text2);
        title = (ImageView) view.findViewById(R.id.dialog_title);
        if (state == SHOWSETIMAGE){
            flag = state;
            confirm.setText(R.string.EFAMILY_LOOK);
            tv1.setText(R.string.EFAMILY_LOOK);
            tv2.setVisibility(View.GONE);
            title.setImageResource(R.drawable.bg_no_photo);
        }
        setContentView(view);
        confirm.setOnClickListener(this);
        setCanceledOnTouchOutside(false);

        window = getWindow();
        window.setWindowAnimations(R.anim.alpha_in);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.low_power_confirm){
            if (flag == SHOWSETIMAGE){
                mContext.startActivity(new Intent(mContext, MyAccount.class));
            }
            dismiss();
        }
    }
}
