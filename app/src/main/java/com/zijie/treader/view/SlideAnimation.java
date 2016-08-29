package com.zijie.treader.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.Scroller;

/**
 * Created by Administrator on 2016/8/29 0029.
 */
public class SlideAnimation extends AnimationProvider {

    public SlideAnimation(Bitmap mCurrentBitmap, Bitmap mNextBitmap, int width, int height) {
        super(mCurrentBitmap, mNextBitmap, width, height);
    }

    @Override
    public void drawMove(Canvas canvas) {

    }

    @Override
    public void drawStatic(Canvas canvas) {

    }

    @Override
    public void startAnimation(Scroller scroller) {

    }

}
