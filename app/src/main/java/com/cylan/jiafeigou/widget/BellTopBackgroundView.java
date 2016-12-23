package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-9-13.
 */
public class BellTopBackgroundView extends FrameLayout {

    TextView tvStartCalling;
    FrameLayout fLayoutBellTopPre;
    FrameLayout fLayoutBellTopNext;
    ViewSwitcher vsBellHomeTop;

    private ActionInterface actionInterface;

    public void setActionInterface(ActionInterface actionInterface) {
        this.actionInterface = actionInterface;
    }

    public BellTopBackgroundView(Context context) {
        this(context, null);
    }

    public BellTopBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BellTopBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View v = LayoutInflater.from(context).inflate(R.layout.layout_bell_home_top_background, this, true);
        tvStartCalling = (TextView) v.findViewById(R.id.tv_start_calling);
        fLayoutBellTopPre = (FrameLayout) v.findViewById(R.id.fLayout_bell_top_pre);
        fLayoutBellTopNext = (FrameLayout) v.findViewById(R.id.fLayout_bell_top_next);
        vsBellHomeTop = (ViewSwitcher) v.findViewById(R.id.vs_bell_home_top);
        tvStartCalling.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionInterface != null)
                    actionInterface.onMakeCall();
            }
        });
    }

    public static final int STATE_BELL_OFFLINE = 0;
    public static final int STATE_BELL_ONLINE = 1;
    public static final int STATE_BAD_NETWORK = 2;

    public int state = 0;

    public void setState(int state) {
        if (this.state == state)
            return;
        this.state = state;
        if (state == 0 || state == 1) {
            if (vsBellHomeTop.getCurrentView() != fLayoutBellTopPre) {
                vsBellHomeTop.showPrevious();
            }
            if (state == 1) {
                tvStartCalling.setVisibility(VISIBLE);
                fLayoutBellTopPre.setBackground(getResources().getDrawable(R.drawable.bg_bell_home_online));
            } else {
                tvStartCalling.setVisibility(GONE);
                fLayoutBellTopPre.setBackground(getResources().getDrawable(R.drawable.bg_bell_home_offline));
            }
        } else {
            if (vsBellHomeTop.getCurrentView() != fLayoutBellTopNext) {
                vsBellHomeTop.showNext();
            }
        }
    }

    public interface ActionInterface {
        void onMakeCall();
    }
}
