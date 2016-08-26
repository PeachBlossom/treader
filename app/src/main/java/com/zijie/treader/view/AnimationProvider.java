package com.zijie.treader.view;

import android.graphics.Bitmap;
import android.graphics.PointF;

/**
 * Created by Administrator on 2016/8/1 0001.
 */
abstract class AnimationProvider {
//    static enum Mode {
//        NoScrolling(false),
//        ManualScrolling(false),
//        AnimatedScrollingForward(true),
//        AnimatedScrollingBackward(true);
//
//        final boolean Auto;
//
//        Mode(boolean auto) {
//            Auto = auto;
//        }
//    }

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

//    private Mode myMode = Mode.NoScrolling;

    private Bitmap mCurrentBitmap,mNextBitmap;
    protected float myStartX;
    protected float myStartY;
    protected int myEndX;
    protected int myEndY;
    protected Direction myDirection;
    protected float mySpeed;

    protected int mScreenWidth;
    protected int mScreenHeight;

    protected PointF mTouch = new PointF(); // 拖拽点
    private Direction direction;
    private boolean isCancel = false;

    public AnimationProvider(Bitmap mCurrentBitmap,Bitmap mNextBitmap,int width,int height) {
        this.mCurrentBitmap = mCurrentBitmap;
        this.mNextBitmap = mNextBitmap;
        this.mScreenWidth = width;
        this.mScreenHeight = height;
    }

    //设置开始拖拽点
    public void setStartPoint(float x,float y){
        myStartX = x;
        myStartY = y;
    }

    //设置拖拽点
    public void setTouchPoint(float x,float y){
        mTouch.x = x;
        mTouch.y = y;
    }

    //设置方向
    public void setDirection(Direction direction){
        this.direction = direction;
    }

    public void setCancel(boolean isCancel){
        this.isCancel = isCancel;
    }

    public Direction getDirection(){
        return direction;
    }
}
