package com.zijie.treader.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zijie.treader.R;
import com.zijie.treader.view.BookPageWidget;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/7/19 0019.
 */
public class ReadSettingDialog implements BaseDialog {
    @Bind(R.id.btn_return)
    ImageButton btn_return;
    @Bind(R.id.btn_light)
    ImageButton btn_ight;
    @Bind(R.id.btn_listener_book)
    ImageButton btn_listener_book;
    @Bind(R.id.tv_blank)
    TextView tv_blank;
    @Bind(R.id.tv_pre)
    TextView tv_pre;
    @Bind(R.id.sb_progress)
    SeekBar sb_progress;
    @Bind(R.id.tv_next)
    TextView tv_next;
    @Bind(R.id.tv_directory)
    TextView tv_directory;
    @Bind(R.id.tv_dayornight)
    TextView tv_dayornight;
    @Bind(R.id.tv_setting)
    TextView tv_setting;

    private PopupWindow mPopupWindow;
    private BookPageWidget mBookPageWidget;
    private View view;
    private SettingListener mSettingListener;


    public ReadSettingDialog(BookPageWidget bookPageWidget) {
        this.mBookPageWidget = bookPageWidget;
        LayoutInflater layoutInflater = (LayoutInflater) bookPageWidget.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.dialog_read_setting, null);
        ButterKnife.bind(this, view);
        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void show() {
        mPopupWindow.showAtLocation(mBookPageWidget, Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    public void dismiss() {
        mPopupWindow.dismiss();
    }

    @Override
    public Boolean isShow() {
        return mPopupWindow.isShowing();
    }

    @OnClick({R.id.btn_return, R.id.btn_light, R.id.btn_listener_book, R.id.tv_blank, R.id.tv_pre, R.id.sb_progress, R.id.tv_next, R.id.tv_directory, R.id.tv_dayornight, R.id.tv_setting})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                if (mSettingListener != null){
                    mSettingListener.back();
                }
                break;
            case R.id.btn_light:
                break;
            case R.id.btn_listener_book:
                break;
            case R.id.tv_blank:
                if (mSettingListener != null){
                    mSettingListener.blank();
                }
                break;
            case R.id.tv_pre:
                if (mSettingListener != null){
                    mSettingListener.pre();
                }
                break;
            case R.id.sb_progress:
                break;
            case R.id.tv_next:
                if (mSettingListener != null){
                    mSettingListener.next();
                }
                break;
            case R.id.tv_directory:
                if (mSettingListener != null){
                    mSettingListener.directory();
                }
                break;
            case R.id.tv_dayornight:
                if (mSettingListener != null){
                    mSettingListener.dayorNight();
                }
                break;
            case R.id.tv_setting:
                if (mSettingListener != null){
                    mSettingListener.setting();
                }
                break;
        }
    }

    public void setSettingListener(SettingListener settingListener){
        this.mSettingListener = settingListener;
    }

    public interface SettingListener{
        void back();
        void blank();
        void pre();
        void next();
        void directory();
        void dayorNight();
        void setting();
    }
}
