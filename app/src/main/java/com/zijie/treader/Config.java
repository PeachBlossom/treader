package com.zijie.treader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;

import com.zijie.treader.util.DisplayUtils;

/**
 * Created by Administrator on 2016/7/18 0018.
 */
public class Config {
    private final static String SP_NAME = "config";
    private final static String FONT_SIZE_KEY = "fontsize";
    private final static String NIGHT_KEY = "night";
    private final static String LIGHT_KEY = "light";
    private final static String SYSTEM_LIGHT_KEY = "systemlight";
    //默认字体大小
    public static int DEFAULT_FONT_SIZE = 0;
    //最小字体大小
    public static int MIN_FONT_SIZE = 0;
    //最大字体大小
    public static int MAX_FONT_SIZE = 0;

    private Context mContext;
    private static Config config;
    private SharedPreferences sp;
    //字体
    private Typeface typeface;
    //字体大小
    private int mFontSize = 0;
    //亮度值
    private float light = 0;

    private Config(Context mContext){
        this.mContext = mContext.getApplicationContext();
        sp = this.mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        DEFAULT_FONT_SIZE = DisplayUtils.sp2px(mContext,20);
        MIN_FONT_SIZE = DisplayUtils.sp2px(mContext,10);
        MAX_FONT_SIZE = DisplayUtils.sp2px(mContext,30);
    }

    public static synchronized Config getInstance(){
        return config;
    }

    public static synchronized Config createConfig(Context context){
        if (config == null){
            config = new Config(context);
        }

        return config;
    }

    public Typeface getTypeface(){
        if (typeface == null) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "font/qihei.ttf");
        }
        return typeface;
    }

    public int getFontSize(){
        if (mFontSize == 0){
            mFontSize = sp.getInt(FONT_SIZE_KEY, DEFAULT_FONT_SIZE);
        }
        return mFontSize;
    }

    public void setFontSize(int fontSize){
        mFontSize = fontSize;
        sp.edit().putInt(FONT_SIZE_KEY,fontSize).commit();
    }

    /**
     * 获取夜间还是白天阅读模式,true为夜晚，false为白天
     */
    public boolean getDayOrNight() {
        return sp.getBoolean(NIGHT_KEY, false);
    }

    public void setDayOrNight(boolean isNight){
        sp.edit().putBoolean(NIGHT_KEY,isNight).commit();
    }

    public Boolean isSystemLight(){
       return sp.getBoolean(SYSTEM_LIGHT_KEY,true);
    }

    public void setSystemLight(Boolean isSystemLight){
        sp.edit().putBoolean(SYSTEM_LIGHT_KEY,isSystemLight).commit();
    }

    public float getLight(){
        if (light == 0){
            light = sp.getFloat(LIGHT_KEY,0.1f);
        }
        return light;
    }
    /**
     * 记录配置文件中亮度值
     */
    public void setLight(float light) {
        this.light = light;
        sp.edit().putFloat(LIGHT_KEY,light).commit();
    }
}
