package com.zijie.treader.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Administrator on 2016/2/5.
 */
public class Rotate3DAnimation extends Animation {
    private Camera mCamera;
    private final float mFromDegrees;
    private final float mToDegrees;
    private float mPivotXValue;// 控件左上角X
    private float mPivotYValue;
    //private final float mDepthZ;   //不需要用到此参数
    private final float scaleTimes;
    private boolean mReverse;

    private float mPivotX;      //缩放点X
    private float mPivotY;      //缩放点Y


    /**
     * cover 动画构造方法，一边放大，一边翻转
     * @param mFromDegrees
     * @param mToDegrees
     * @param mPivotXValue  控件左上角X
     * @param mPivotYValue  控件左上角Y
     * @param scaleTimes    缩放比例
     * @param mReverse   动画是否逆向进行
     */
    public Rotate3DAnimation(float mFromDegrees, float mToDegrees, float mPivotXValue, float mPivotYValue, float scaleTimes, boolean mReverse) {
        this.mFromDegrees = mFromDegrees;
        this.mToDegrees = mToDegrees;
        this.mPivotXValue = mPivotXValue;
        this.mPivotYValue = mPivotYValue;
        this.scaleTimes = scaleTimes;
        this.mReverse = mReverse;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
        mPivotX = resolvePivotX(mPivotXValue, parentWidth, width);  //计算缩放点X
        mPivotY = resolvePivoY(mPivotYValue, parentHeight, height);   //计算缩放点Y
    }

    /**
     * 执行顺序 matrix.preTranslate() -->  camera.rotateY(degrees) -->   matrix.postTranslate() -->   matrix.postScale()
     * @param interpolatedTime
     * @param t
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float degrees = mReverse ? mToDegrees + (mFromDegrees - mToDegrees) * interpolatedTime : mFromDegrees + (mToDegrees - mFromDegrees) * interpolatedTime;
        final Matrix matrix = t.getMatrix();

        final Camera camera = mCamera;

        camera.save();

        camera.rotateY(degrees);

        camera.getMatrix(matrix);
        camera.restore();

              //  matrix.preTranslate(-mPivotXValue, 0);      //在进行rotateY之前需要移动物体，让物体左边与Y轴对齐
              //  matrix.postTranslate(mPivotXValue, 0);      //还原物体位置

        if (mReverse) {
            matrix.postScale(1 + (scaleTimes - 1) * (1.0f - interpolatedTime), 1 + (scaleTimes - 1) * (1.0f - interpolatedTime), mPivotX - mPivotXValue , mPivotY - mPivotYValue);
        } else {
           // matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX, mPivotY);
            matrix.postScale(1 + (scaleTimes - 1) * interpolatedTime, 1 + (scaleTimes - 1) * interpolatedTime, mPivotX - mPivotXValue , mPivotY - mPivotYValue );
        }
    }

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
