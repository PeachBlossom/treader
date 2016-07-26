package com.zijie.treader.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zijie.treader.Config;
import com.zijie.treader.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/7/26 0026.
 */
public class SettingDialog extends Dialog {

    @Bind(R.id.tv_dark)
    TextView tv_dark;
    @Bind(R.id.sb_brightness)
    SeekBar sb_brightness;
    @Bind(R.id.tv_bright)
    TextView tv_bright;
    @Bind(R.id.tv_xitong)
    TextView tv_xitong;
    @Bind(R.id.tv_subtract)
    TextView tv_subtract;
    @Bind(R.id.tv_size)
    TextView tv_size;
    @Bind(R.id.tv_add)
    TextView tv_add;
    @Bind(R.id.tv_qihei)
    TextView tv_qihei;
    @Bind(R.id.tv_xinshou)
    TextView tv_xinshou;
    @Bind(R.id.tv_wawa)
    TextView tv_wawa;

    private Config config;
    private Boolean isSystem;
    private SettingListener mSettingListener;

    private SettingDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    public SettingDialog(Context context) {
        this(context, R.style.setting_dialog);
    }

    public SettingDialog(Context context, int themeResId) {
        super(context, themeResId);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        applyCompat();
        getWindow().setGravity(Gravity.BOTTOM);
        setContentView(R.layout.dialog_setting);
        // 初始化View注入
        ButterKnife.bind(this);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);

        config = Config.getInstance();
        isSystem = config.isSystemLight();
        setTextViewSelect(tv_xitong,isSystem);
        setBrightness(config.getLight());

        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 10) {
                    changeBright(false, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //设置亮度
    public void setBrightness(float brightness){
        sb_brightness.setProgress((int) (brightness * 100));
    }

   //设置按钮选择的背景
    private void setTextViewSelect(TextView textView,Boolean isSelect){
        if (isSelect){
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_select_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.read_dialog_button_select));
        }else{
            textView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.button_bg));
            textView.setTextColor(getContext().getResources().getColor(R.color.white));
        }
    }

    private void applyCompat() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
    }

    public Boolean isShow() {
        return isShowing();
    }


    @OnClick({R.id.tv_dark, R.id.tv_bright, R.id.tv_xitong, R.id.tv_subtract, R.id.tv_add, R.id.tv_qihei, R.id.tv_xinshou, R.id.tv_wawa})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dark:
                break;
            case R.id.tv_bright:
                break;
            case R.id.tv_xitong:
                isSystem = !isSystem;
                changeBright(isSystem,sb_brightness.getProgress());
                break;
            case R.id.tv_subtract:
                break;
            case R.id.tv_add:
                break;
            case R.id.tv_qihei:
                break;
            case R.id.tv_xinshou:
                break;
            case R.id.tv_wawa:
                break;
        }
    }

    //改变亮度
    public void changeBright(Boolean isSystem,int brightness){
        float light = (float) (brightness / 100.0);
        setTextViewSelect(tv_xitong,isSystem);
        config.setSystemLight(isSystem);
        config.setLight(light);
        if (mSettingListener != null){
            mSettingListener.changeSystemBright(isSystem, light);
        }
    }

    public void setSettingListener(SettingListener settingListener){
        this.mSettingListener = settingListener;
    }

    public interface SettingListener{
        void changeSystemBright(Boolean isSystem,float brightness);
    }
}