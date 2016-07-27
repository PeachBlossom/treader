package com.zijie.treader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookList;
import com.zijie.treader.dialog.ReadSettingDialog;
import com.zijie.treader.dialog.SettingDialog;
import com.zijie.treader.util.BrightnessUtil;
import com.zijie.treader.util.PageFactory;
import com.zijie.treader.util.PageFactory1;
import com.zijie.treader.view.BookPageWidget;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/7/15 0015.
 */
public class ReadActivity1 extends BaseActivity {
    private static final String TAG = "ReadActivity";
    private final static String EXTRA_BOOK = "bookList";
    private final static int MESSAGE_CHANGEPROGRESS = 1;

    @Bind(R.id.bookpage)
    BookPageWidget bookpage;

    private Config config;
    private WindowManager.LayoutParams lp;
    private BookList bookList;
    private PageFactory1 pageFactory;
    private int screenWidth,screenHeight;
    // popwindow是否显示
    private Boolean isShow = false;
    private ReadSettingDialog mReadSettingDialog;
    private SettingDialog mSettingDialog;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_read1;
    }

    @Override
    protected void initData() {
        config = Config.getInstance();
        pageFactory = PageFactory1.getInstance();

        mReadSettingDialog = new ReadSettingDialog(bookpage);
        mSettingDialog = new SettingDialog(this);
        //获取屏幕宽高
        WindowManager manage = getWindowManager();
        Display display = manage.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);
        screenWidth = displaysize.x;
        screenHeight = displaysize.y;
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏
        hideSystemUI();
        //改变屏幕亮度
        if (!config.isSystemLight()){
            BrightnessUtil.setBrightness(this,config.getLight());
        }
        //获取intent中的携带的信息
        Intent intent = getIntent();
        bookList = (BookList) intent.getSerializableExtra(EXTRA_BOOK);

        pageFactory.setPageWidget(bookpage);

        try {
            pageFactory.openBook(bookList.getBookpath(), (int) bookList.getBegin());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "打开电子书失败", Toast.LENGTH_SHORT).show();
        }
        
    }

    @Override
    protected void initListener() {
                    mSettingDialog.setSettingListener(new SettingDialog.SettingListener() {
            @Override
            public void changeSystemBright(Boolean isSystem, float brightness) {
                if (!isSystem){
                    BrightnessUtil.setBrightness(ReadActivity1.this,brightness);
                }else{
                    int bh = BrightnessUtil.getScreenBrightness(ReadActivity1.this);
                    BrightnessUtil.setBrightness(ReadActivity1.this,bh);
                }
            }

            @Override
            public void changeFontSize(int fontSize) {
                pageFactory.changeFontSize(fontSize);
            }

            @Override
            public void changeTypeFace(Typeface typeface) {
                pageFactory.changeTypeface(typeface);
            }

            @Override
            public void changeBookBg(int type) {
                pageFactory.changeBookBg(type);
            }
        });

        pageFactory.setPageEvent(new PageFactory1.PageEvent() {
            @Override
            public void changeProgress(float progress) {
                Message message = new Message();
                message.what = MESSAGE_CHANGEPROGRESS;
                message.obj = progress;
                mHandler.sendMessage(message);
            }
        });

        mReadSettingDialog.setSettingListener(new ReadSettingDialog.SettingListener() {
            @Override
            public void back() {
                finish();
            }

            @Override
            public void blank() {
                hideSystemUI();
                mReadSettingDialog.dismiss();
                isShow = false;
            }

            @Override
            public void pre() {

            }

            @Override
            public void next() {

            }

            @Override
            public void directory() {

            }

            @Override
            public void dayorNight(Boolean isNight) {
                pageFactory.setDayOrNight(isNight);
            }

            @Override
            public void setting() {
                mReadSettingDialog.dismiss();
                isShow = false;
                mSettingDialog.show();
            }

            @Override
            public void changeProgress(float progress) {
                pageFactory.changeProgress(progress);
            }
        });

        bookpage.setTouchListener(new BookPageWidget.TouchListener() {
            @Override
            public void center() {
                if (isShow){
                    hideSystemUI();
                    mReadSettingDialog.dismiss();
                    isShow = false;
                }else{
                    showSystemUI();
                    mReadSettingDialog.show();
                    isShow = true;
                }
            }

            @Override
            public Boolean prePage() {
                pageFactory.prePage();
                if (pageFactory.isfirstPage()) {
                    return false;
                }

                return true;
            }

            @Override
            public Boolean nextPage() {
                pageFactory.nextPage();
                if (pageFactory.islastPage()) {
                    return false;
                }
                return true;
            }
        });

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MESSAGE_CHANGEPROGRESS:
                    float progress = (float) msg.obj;
                    mReadSettingDialog.setSeekBarProgress(progress);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReadSettingDialog.isShow()){
            mReadSettingDialog.dismiss();
            mReadSettingDialog = null;
        }
        pageFactory.clear();
    }

    public static void openBook(BookList bookList, Activity context){
        Intent intent = new Intent(context,ReadActivity1.class);
        intent.putExtra(EXTRA_BOOK,bookList);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        context.overridePendingTransition(android.support.v7.appcompat.R.anim.abc_grow_fade_in_from_bottom, android.support.v7.appcompat.R.anim.abc_shrink_fade_out_from_bottom);
    }

    public BookPageWidget getPageWidget(){
        return bookpage;
    }

    /**
     * 隐藏菜单。沉浸式阅读
     */
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

}
