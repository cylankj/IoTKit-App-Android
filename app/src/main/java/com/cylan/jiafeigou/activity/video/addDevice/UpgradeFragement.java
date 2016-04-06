package com.cylan.jiafeigou.activity.video.addDevice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpgradeFragement.OnUpgradeButtonClickListener} interface
 * to handle interaction events.
 * Use the {@link UpgradeFragement#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpgradeFragement extends Fragment implements View.OnClickListener {


    private OnUpgradeButtonClickListener mListener;

    public static UpgradeFragement newInstance() {
        return new UpgradeFragement();
    }


    private ImageView mUpgradeView1;
    private ImageView mUpgradeView2;
    private TextView mUpgradeTextView;
    private Button mUpgradeBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_ucos_upgrade, container, false);
        mUpgradeView1 = (ImageView) view.findViewById(R.id.img_upgrade);
        mUpgradeView2 = (ImageView) view.findViewById(R.id.img_complete);
        mUpgradeTextView = (TextView) view.findViewById(R.id.upgrade_des);
        mUpgradeTextView.setText(getString(R.string.UPDATING_LABEL) + "(0%)");
        mUpgradeBtn = (Button) view.findViewById(R.id.update_btn);
        mUpgradeBtn.setOnClickListener(this);
        mUpgradeBtn.setEnabled(false);
        return view;
    }


    @Override
    public void onClick(View v) {
        if (mListener != null)
            mListener.OnUpgradeButtonClick();
    }


    public void setOnUpgradeButtonClickListener(OnUpgradeButtonClickListener listener) {
        mListener = listener;
    }

    public interface OnUpgradeButtonClickListener {
        void OnUpgradeButtonClick();
    }

    public void setUpgradeTextViewText(String text) {
        if (mUpgradeTextView != null)
            mUpgradeTextView.setText(text);

    }

    public void setUpgradeTextViewText(int text) {
        if (mUpgradeTextView != null)
            mUpgradeTextView.setText(text);

    }

    public void showUpgradeAnimation(int id) {
        setUpgradeTextViewText(R.string.RE_ADD_LABEL);
        if (mUpgradeBtn != null) {
            mUpgradeBtn.setEnabled(true);
            mUpgradeBtn.setText(id);
        }
        if (mUpgradeView2 != null && mUpgradeView1 != null) {
            mUpgradeView2.setVisibility(View.VISIBLE);
            mUpgradeView2.setAlpha(0.0f);
            mUpgradeView2.setImageResource(R.drawable.pic_ucos_upgrade_complete);
            ObjectAnimator animator = ObjectAnimator.ofFloat(mUpgradeView1, "alpha", 1.0f, 0.0f).setDuration(800);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUpgradeView1.setVisibility(View.INVISIBLE);
                }
            });
            animator.start();
            ObjectAnimator.ofFloat(mUpgradeView2, "alpha", 0.0f, 1.0f).setDuration(800).start();
        }
    }
}
