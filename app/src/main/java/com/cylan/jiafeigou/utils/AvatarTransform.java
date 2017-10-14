package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by yanzhendong on 2017/10/13.
 */

public class AvatarTransform extends BitmapTransformation {
    public AvatarTransform(Context context,String[]coord) {
        super(context);
    }

    public AvatarTransform(BitmapPool bitmapPool) {
        super(bitmapPool);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

        return null;
    }

    @Override
    public String getId() {
        return "AvatarTransform.com.cylan.jiafeigou.utils";
    }
}
