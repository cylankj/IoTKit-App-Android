package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.cylan.jiafeigou.R;

public class EditDelText extends EditText {
	Drawable[] drawables;
	Drawable drawableDel;
	boolean hideDelForce = false;

	public EditDelText(Context context) {
		super(context);
	}

	public EditDelText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditDelText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		drawables = getCompoundDrawables();
		drawableDel = getResources().getDrawable(R.drawable.ic_input_del);
	}

	public void showDelAll() {
		setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], hideDelForce ? null : drawableDel, drawables[3]);
	}

	protected void hideDelAll() {
		setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], null, drawables[3]);
	}

	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		if (focused) {
			if (getText().length() > 0) {
				showDelAll();
			}
		} else {
			hideDelAll();
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before, int after) {
		if (!hasFocus()) {
			return;
		}
		if (text.length() > 0) {
			showDelAll();
		} else {
			hideDelAll();
		}
		super.onTextChanged(text, start, before, after);
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			Drawable[] drawables = getCompoundDrawables();
			Drawable right = drawables[2];
			if (right == null) {
				break;
			}
			Rect rect = right.getBounds();
			if (rect.contains(getWidth() - getPaddingRight() - (int) event.getX(), getHeight() - getPaddingBottom() - (int) event.getY())) {
				setText("");
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	public void setHideDelForce(boolean force) {
		hideDelForce = force;
	}
}