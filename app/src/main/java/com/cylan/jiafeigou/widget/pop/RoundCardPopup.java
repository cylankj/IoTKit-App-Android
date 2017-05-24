package com.cylan.jiafeigou.widget.pop;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 17-2-23.
 */

public class RoundCardPopup extends RelativePopupWindow {
    private ShowFinishListener showFinishListener;
    private long dismissTime = -1;

    public RoundCardPopup(Context context, ShowFinishListener showFinishListener, View.OnClickListener clickListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_capture_popup, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }
        this.showFinishListener = showFinishListener;
        if (clickListener != null)
            view.setOnClickListener(clickListener);
    }

    @Override
    public void showOnAnchor(@NonNull View anchor, int vertPos, int horizPos, int x, int y) {
        super.showOnAnchor(anchor, vertPos, horizPos, x, y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            circularReveal(anchor);
        }
        if (dismissTime != -1)
            getContentView().postDelayed(this::dismiss, dismissTime);
        if (showFinishListener != null)
            showFinishListener.showFinish((ImageView) getContentView().findViewById(R.id.imv_capture_popup));
    }

    public void setAutoDismissTime(long time) {
        this.dismissTime = time;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularReveal(@NonNull final View anchor) {
        final View contentView = getContentView();
        contentView.post(new Runnable() {
            @Override
            public void run() {
                final int[] myLocation = new int[2];
                final int[] anchorLocation = new int[2];
                contentView.getLocationOnScreen(myLocation);
                anchor.getLocationOnScreen(anchorLocation);
                final int cx = anchorLocation[0] - myLocation[0] + anchor.getWidth() / 2;
                final int cy = anchorLocation[1] - myLocation[1] + anchor.getHeight() / 2;

                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                final int dx = Math.max(cx, contentView.getMeasuredWidth() - cx);
                final int dy = Math.max(cy, contentView.getMeasuredHeight() - cy);
                final float finalRadius = (float) Math.hypot(dx, dy);
                Animator animator = ViewAnimationUtils.createCircularReveal(contentView, cx, cy, 0f, finalRadius);
                animator.setDuration(500);
                animator.start();
            }
        });
    }

    public interface ShowFinishListener {
        void showFinish(ImageView imageView);
    }
}
