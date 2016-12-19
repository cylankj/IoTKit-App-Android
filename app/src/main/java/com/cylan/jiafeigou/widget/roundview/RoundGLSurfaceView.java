package com.cylan.jiafeigou.widget.roundview;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by yzd on 16-12-19.
 */

public class RoundGLSurfaceView extends GLSurfaceView {

    private RoundedTextureView.GLRenderer glRenderer;

    public RoundGLSurfaceView(Context context) {
        super(context);
    }

    public RoundGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
    }


}
