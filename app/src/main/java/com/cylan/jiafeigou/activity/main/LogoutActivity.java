package com.cylan.jiafeigou.activity.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.AppManager;

/**
 * Created by hebin on 2015/8/1.
 */
public class LogoutActivity extends Activity {

    private static LogoutListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_logout);
        AppManager.getAppManager().addActivity(this);

        TextView btncancel = (TextView) findViewById(R.id.btn_cancel);
        Button logout = (Button) findViewById(R.id.logout_confirm);


        btncancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.logout();
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }


    public static void setOnLogoutListener(LogoutListener listener) {
        mListener = listener;
    }

    public interface LogoutListener {
        void logout();
    }
}

