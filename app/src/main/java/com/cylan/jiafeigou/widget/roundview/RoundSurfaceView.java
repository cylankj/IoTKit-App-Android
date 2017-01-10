package com.cylan.jiafeigou.widget.roundview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by yzd on 16-12-19.
 */

public class RoundSurfaceView extends SurfaceView {
    public RoundSurfaceView(Context context) {
        super(context);
    }

    public RoundSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void draw(Canvas canvas) {

        Path path = new Path();
        //用矩形表示SurfaceView宽高
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        //15.0f即是圆角半径
        path.addRoundRect(rect, 20.0f, 20.0f, Path.Direction.CCW);
        //裁剪画布，并设置其填充方式
        canvas.clipPath(path, Region.Op.REPLACE);
        super.draw(canvas);
    }
}
