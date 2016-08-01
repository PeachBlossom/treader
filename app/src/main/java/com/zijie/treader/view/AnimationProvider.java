package com.zijie.treader.view;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/8/1 0001.
 */
abstract class AnimationProvider {
    static enum Mode {
        NoScrolling(false),
        ManualScrolling(false),
        AnimatedScrollingForward(true),
        AnimatedScrollingBackward(true);

        final boolean Auto;

        Mode(boolean auto) {
            Auto = auto;
        }
    }

    public static enum Direction {
        leftToRight(true), rightToLeft(true), up(false), down(false);

        public final boolean IsHorizontal;

        Direction(boolean isHorizontal) {
            IsHorizontal = isHorizontal;
        }
    };
    public static enum Animation {
        none, curl, slide, shift
    }

    private Mode myMode = Mode.NoScrolling;

    private Bitmap mCurrentBitmap,mNextBitmap;
    protected int myStartX;
    protected int myStartY;
    protected int myEndX;
    protected int myEndY;
    protected Direction myDirection;
    protected float mySpeed;

    protected int myWidth;
    protected int myHeight;

    protected AnimationProvider(Bitmap mCurrentBitmap,Bitmap mNextBitmap,int width,int height) {
        this.mCurrentBitmap = mCurrentBitmap;
        this.mNextBitmap = mNextBitmap;
        this.myWidth = width;
        this.myHeight = height;
    }


}
