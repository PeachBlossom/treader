package com.zijie.treader.animation;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Administrator on 2016/2/5.
 */
public class ContentScaleAnimation extends Animation {
    private float mPivotX;
    private float mPivotY;
    private float mPivotXValue; // 控件左上角X
    private float mPivotYValue;
    private final float scaleTimes;
    private boolean mReverse;

    public ContentScaleAnimation(float mPivotXValue, float mPivotYValue, float scaleTimes, boolean mReverse) {

        this.mPivotXValue = mPivotXValue;
        this.mPivotYValue = mPivotYValue;
        this.scaleTimes = scaleTimes;
        this.mReverse = mReverse;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        Matrix matrix=t.getMatrix();//缩放方法
        if (mReverse) {
            matrix.postScale(1 + (scaleTimes - 1) * (1.0f - interpolatedTime), 1 + (scaleTimes - 1) * (1.0f - interpolatedTime), mPivotX - mPivotXValue, mPivotY - mPivotYValue);
        } else {
          //  matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX, mPivotY);
            matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX - mPivotXValue , mPivotY - mPivotYValue );
        }
        float a = mPivotX - mPivotXValue;
        float b = mPivotY - mPivotYValue;
       // Log.d("scaleAnimation","mPivotX - mPivotXValue ="+a);
       // Log.d("scaleAnimation","mPivotY - mPivotYValue ="+b);
    }
    //缩放点坐标值
    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolvePivotX(mPivotXValue, parentWidth, width);
        mPivotY = resolvePivoY(mPivotYValue, parentHeight, height);
       // Log.d("scaleAnimation","mPivotX is"+mPivotX);
      //  Log.d("scaleAnimation","mPivotY is"+mPivotY);
       // Log.d("scaleAnimation","mPivotXValue is"+mPivotXValue);
      //  Log.d("scaleAnimation","mPivotYValue is"+mPivotYValue);
      //  Log.d("scaleAnimation","width ="+width);
       // Log.d("scaleAnimation","width ="+height);
       // Log.d("scaleAnimation","width ="+parentWidth);
      //  Log.d("scaleAnimation","width ="+parentHeight);
    }
    //缩放点坐标值   缩放点到自身左边距离/缩放点到父控件左边的距离=缩放点自身右侧距离/缩放点到父控件右边的距离
    private float resolvePivotX(float margingLeft, int parentWidth, int width) {
        return (margingLeft * parentWidth) / (parentWidth - width);
    }

    private float resolvePivoY(float marginTop, int parentHeight, int height) {

        return (marginTop * parentHeight) / (parentHeight - height);
    }

    public void reverse() {
        mReverse = !mReverse;
    }

    public boolean getMReverse() {
        return mReverse;
    }

    public void setmPivotXValue (float mPivotXValue1) {
        this.mPivotXValue = mPivotXValue1;
    }

    public void setmPivotYValue (float mPivotYValue1) {
        this.mPivotYValue = mPivotYValue1;
    }
}
