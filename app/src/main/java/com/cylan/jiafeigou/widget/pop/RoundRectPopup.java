package com.cylan.jiafeigou.widget.pop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SafeRadioButton;

/**
 * Created by cylan-hunt on 17-2-23.
 */

public class RoundRectPopup extends RelativePopupWindow {
    SafeRadioButton menuItemAlbumPopBoth;
    SafeRadioButton menuItemAlbumPopPanorama;
    SafeRadioButton menuItemAlbumPopPhoto;

    RadioGroup menuAlbumPopContainer;

    private long dismissTime = -1;
    private int checkedIndex = -1;

    public RoundRectPopup(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_panorama_album_pop_menu, null);
        menuAlbumPopContainer = (RadioGroup) view.findViewById(R.id.menu_album_pop_container);
        menuItemAlbumPopBoth = (SafeRadioButton) view.findViewById(R.id.menu_item_album_pop_both);
        menuItemAlbumPopPanorama = (SafeRadioButton) view.findViewById(R.id.menu_item_album_pop_panorama);
        menuItemAlbumPopPhoto = (SafeRadioButton) view.findViewById(R.id.menu_item_album_pop_photo);
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
    }

    public void setCheckIndex(int index) {
        this.checkedIndex = index;
    }

    @Override
    public void showOnAnchor(@NonNull View anchor, int vertPos, int horizPos, int x, int y) {
        super.showOnAnchor(anchor, vertPos, horizPos, x, y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            circularReveal(anchor);
        }
//        if (dismissTime != -1)
//            getContentView().postDelayed(this::dismiss, dismissTime);
        if (checkedIndex != -1) {
            menuItemAlbumPopBoth.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                dismiss();
                if (dismissListener != null)
                    dismissListener.onDismiss(R.id.menu_item_album_pop_both);
            });
            menuItemAlbumPopPanorama.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                dismiss();
                if (dismissListener != null)
                    dismissListener.onDismiss(R.id.menu_item_album_pop_panorama);
            });
            menuItemAlbumPopPhoto.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                dismiss();
                if (dismissListener != null)
                    dismissListener.onDismiss(R.id.menu_item_album_pop_photo);
            });
            //只能放在后面
            ((SafeRadioButton) menuAlbumPopContainer.getChildAt(checkedIndex)).setChecked(true, false);
        }
    }

    //    public void setAutoDismissTime(long time) {
//        this.dismissTime = time;
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void circularReveal(@NonNull final View anchor) {
//        final View contentView = getContentView();
//        final int[] myLocation = new int[2];
//        final int[] anchorLocation = new int[2];
//        contentView.getLocationOnScreen(myLocation);
//        anchor.getLocationOnScreen(anchorLocation);
//        final int cx = anchorLocation[0] - myLocation[0] + anchor.getWidth() / 2;
//        final int cy = anchorLocation[1] - myLocation[1] + anchor.getHeight() / 2;
//        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        final int dx = Math.max(cx, contentView.getMeasuredWidth() - cx);
//        final int dy = Math.max(cy, contentView.getMeasuredHeight() - cy);
//        final float finalRadius = (float) Math.hypot(dx, dy);
//        Animator animator = ViewAnimationUtils.createCircularReveal(contentView, cx, cy, 0f, finalRadius);
//        animator.setDuration(500);
//        animator.start();
//    }
    private DismissListener dismissListener;

    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int id);
    }
}
