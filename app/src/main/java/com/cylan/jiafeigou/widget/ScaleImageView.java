package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by cylan on 2015/10/10.
 */
public class ScaleImageView extends ImageView {

    private static final String TAG = ScaleImageView.class.getSimpleName();

    private static final float MAX_SCALE = 2.4f;

    private Matrix matrix = new Matrix();


    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isMatrixEnable();
        matrix.setScale(1.0f, 1.0f, getWidth() / 2, getHeight() / 2);
        setImageMatrix(matrix);
    }

    /**
     * 设置缩放Matrix
     *
     * @param scale
     */
    public void setDragMatrix(float scale) {
        Log.d(TAG, "width-->" + getWidth() + "\theight-->" + getHeight());
//        matrix.setScale(scale, scale, getWidth() / 2, getHeight() / 2);
//        setImageMatrix(matrix);
        setScaleX(scale);
        setScaleY(scale);
    }

    private float checkMaxScale(float scale, float[] values) {
        if (scale * values[Matrix.MSCALE_X] > MAX_SCALE)
            scale = MAX_SCALE / values[Matrix.MSCALE_X];
        matrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
        return scale;
    }

    /**
     * 重置Matrix
     */
    public void resetMatrix() {
        //matrix.setScale(1.0f, 1.0f);
        //setImageMatrix(matrix);
        setScaleX(1.0f);
        setScaleY(1.0f);
    }


    /**
     * 判断是否支持Matrix
     */
    public void isMatrixEnable() {
        if (getScaleType() != ScaleType.CENTER) {
            setScaleType(ScaleType.MATRIX);
        }
    }
}
