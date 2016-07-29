package com.zijie.treader.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zijie.treader.Config;
import com.zijie.treader.R;
import com.zijie.treader.view.BookPageWidget;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/7/19 0019.
 */
public class ReadSettingDialog implements BaseDialog,View.OnClickListener {

    ImageButton btn_return;
    ImageButton btn_ight;
    ImageButton btn_listener_book;
    TextView tv_pre;
    SeekBar sb_progress;
    TextView tv_next;
    TextView tv_directory;
    TextView tv_dayornight;
    TextView tv_setting;
    TextView tv_Progress;
    RelativeLayout rl_Progress;

    private PopupWindow mPopupWindow,mPopupWindowTop;
    private BookPageWidget mBookPageWidget;
    private View view,viewTop;
    private SettingListener mSettingListener;
    private Context mContext;
    private Config config;
    private Boolean mDayOrNight;

    public ReadSettingDialog(BookPageWidget bookPageWidget) {
        this.mBookPageWidget = bookPageWidget;
        mContext = bookPageWidget.getContext();
        LayoutInflater layoutInflater = (LayoutInflater) bookPageWidget.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.dialog_read_setting, null);
        viewTop = layoutInflater.inflate(R.layout.dialog_read_setting_top, null);

        btn_return = (ImageButton) viewTop.findViewById(R.id.btn_return);
        btn_ight = (ImageButton) viewTop.findViewById(R.id.btn_light);
        btn_listener_book = (ImageButton) viewTop.findViewById(R.id.btn_listener_book);
        tv_pre = (TextView) view.findViewById(R.id.tv_pre);
        sb_progress = (SeekBar) view.findViewById(R.id.sb_progress);
        tv_next = (TextView) view.findViewById(R.id.tv_next);
        tv_directory = (TextView) view.findViewById(R.id.tv_directory);
        tv_dayornight = (TextView) view.findViewById(R.id.tv_dayornight);
        tv_setting = (TextView) view.findViewById(R.id.tv_setting);
        tv_Progress = (TextView) view.findViewById(R.id.tv_progress);
        rl_Progress = (RelativeLayout) view.findViewById(R.id.rl_progress);

        btn_return.setOnClickListener(this);
        btn_ight.setOnClickListener(this);
        btn_listener_book.setOnClickListener(this);
        tv_pre.setOnClickListener(this);
        sb_progress.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        tv_directory.setOnClickListener(this);
        tv_dayornight.setOnClickListener(this);
        tv_setting.setOnClickListener(this);
        tv_Progress.setOnClickListener(this);
        rl_Progress.setOnClickListener(this);


        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindowTop = new PopupWindow(viewTop, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);// menu菜单获得焦点 如果没有获得焦点menu菜单中的控件事件无法响应
        mPopupWindow.update();

        mPopupWindowTop.setOutsideTouchable(true);
        mPopupWindowTop.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindowTop.setOutsideTouchable(true);
        mPopupWindowTop.setFocusable(true);// menu菜单获得焦点 如果没有获得焦点menu菜单中的控件事件无法响应
        mPopupWindowTop.update();

        view.setOnTouchListener(new View.OnTouchListener()// 需要设置，点击之后取消popupview，即使点击外面，也可以捕获事件
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (isShow())
                {
                    dismiss();
                }
                return false;
            }
        });


        config = Config.getInstance();

        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float pro;
            // 触发操作，拖动
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = (float) (progress / 10000.0);
                showProgress(pro);
            }

            // 表示进度条刚开始拖动，开始拖动时候触发的操作
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            // 停止拖动时候
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mSettingListener != null){
                    mSettingListener.changeProgress(pro);
                }
            }
        });

        initDayOrNight();
    }

    public void initDayOrNight(){
        mDayOrNight = config.getDayOrNight();
        if (mDayOrNight){
            tv_dayornight.setText(mContext.getResources().getString(R.string.read_setting_day));
        }else{
            tv_dayornight.setText(mContext.getResources().getString(R.string.read_setting_night));
        }
    }

    //改变显示模式
    public void changeDayOrNight(){
        if (mDayOrNight){
            mDayOrNight = false;
            tv_dayornight.setText(mContext.getResources().getString(R.string.read_setting_night));
        }else{
            mDayOrNight = true;
            tv_dayornight.setText(mContext.getResources().getString(R.string.read_setting_day));
        }
        config.setDayOrNight(mDayOrNight);
        if (mSettingListener != null) {
            mSettingListener.dayorNight(mDayOrNight);
        }
    }

    //显示书本进度
    public void showProgress(float progress){
        if (rl_Progress.getVisibility() != View.VISIBLE) {
            rl_Progress.setVisibility(View.VISIBLE);
        }
        setProgress(progress);
    }

    //隐藏书本进度
    public void hideProgress(){
        rl_Progress.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        hideProgress();
        mPopupWindowTop.showAtLocation(mBookPageWidget, Gravity.TOP, 0, 0);
        mPopupWindow.showAtLocation(mBookPageWidget, Gravity.BOTTOM, 0, 0);
    }

    private void setProgress(float progress){
        DecimalFormat decimalFormat=new DecimalFormat("00.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p=decimalFormat.format(progress * 100.0);//format 返回的是字符串
        tv_Progress.setText(p + "%");
    }

    public void setSeekBarProgress(float progress){
        sb_progress.setProgress((int) (progress * 10000));
    }

    @Override
    public void dismiss() {
        mPopupWindowTop.dismiss();
        mPopupWindow.dismiss();
    }

    @Override
    public Boolean isShow() {
        return mPopupWindow.isShowing() || mPopupWindowTop.isShowing();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                if (mSettingListener != null) {
                    mSettingListener.back();
                }
                break;
            case R.id.btn_light:
                break;
            case R.id.btn_listener_book:
                break;
            case R.id.tv_pre:
                if (mSettingListener != null) {
                    mSettingListener.pre();
                }
                break;
            case R.id.sb_progress:

                break;
            case R.id.tv_next:
                if (mSettingListener != null) {
                    mSettingListener.next();
                }
                break;
            case R.id.tv_directory:
                if (mSettingListener != null) {
                    mSettingListener.directory();
                }
                break;
            case R.id.tv_dayornight:
                changeDayOrNight();
                break;
            case R.id.tv_setting:
                if (mSettingListener != null) {
                    mSettingListener.setting();
                }
                break;
        }
    }

    public void setSettingListener(SettingListener settingListener) {
        this.mSettingListener = settingListener;
    }

    public interface SettingListener {
        void back();

        void pre();

        void dismiss();

        void next();

        void directory();

        void dayorNight(Boolean isNight);

        void setting();

        void changeProgress(float progress);
    }
}
