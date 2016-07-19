package com.zijie.treader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.db.BookList;
import com.zijie.treader.dialog.ReadSettingDialog;
import com.zijie.treader.util.PageFactory;
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

    @Bind(R.id.bookpage)
    BookPageWidget bookpage;

    private Config config;
    private WindowManager.LayoutParams lp;
    private BookList bookList;
    private PageFactory pageFactory;
    private int screenWidth,screenHeight;
    // popwindow是否显示
    private Boolean isShow = false;
    private ReadSettingDialog mReadSettingDialog;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_read1;
    }

    @Override
    protected void initData() {
        config = Config.getInstance();
        pageFactory = PageFactory.getInstance();

        mReadSettingDialog = new ReadSettingDialog(bookpage);
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
//        lp = getWindow().getAttributes();
//        lp.screenBrightness = config.getLight() / 10.0f < 0.01f ? 0.01f : config.getLight() / 10.0f;
//        getWindow().setAttributes(lp);
        //获取intent中的携带的信息
        Intent intent = getIntent();
        bookList = (BookList) intent.getSerializableExtra(EXTRA_BOOK);

        pageFactory.setPageWidget(bookpage);

        try {
            pageFactory.openbook(bookList.getBookpath(), (int) bookList.getBegin());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "打开电子书失败", Toast.LENGTH_SHORT).show();
        }
        
    }

    @Override
    protected void initListener() {
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
            public void dayorNight() {

            }

            @Override
            public void setting() {

            }
        });

        bookpage.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (v.equals(bookpage)) {
//                    if (!bookpage.isFinishAnim()){
//                        return false;
//                    }
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        int x = (int) e.getX();
                        int y = (int) e.getY();
                        //Action_Down时在中间位置显示菜单
                        if (x > screenWidth / 3 && x < screenWidth * 2 / 3 && y > screenHeight / 3 && y < screenHeight * 2 / 3) {
//                            if (!bookpage.isFinishAnim()){
//                                return false;
//                            }
                            if (isShow){
                                hideSystemUI();
                                mReadSettingDialog.dismiss();
                                isShow = false;
                            }else{
                                showSystemUI();
                                mReadSettingDialog.show();
                                isShow = true;
                            }
                            return false;//停止向下分发事件
                        }
                        bookpage.abortAnimation();
                        bookpage.calcCornerXY(e.getX(), e.getY());
                        if (x < screenWidth / 2) {// 从左翻
                            try {
                                pageFactory.prePage();
                            } catch (IOException e1) {
                                Log.e(TAG, "onTouch->prePage error", e1);
                            }
                            if (pageFactory.isfirstPage()) {
                                return false;
                            }

                        } else if (x >= screenWidth / 2) {// 从右翻
                            try {
                                pageFactory.nextPage();
                            } catch (IOException e1) {
                                Log.e(TAG, "onTouch->nextPage error", e1);
                            }
                            if (pageFactory.islastPage()) {
                                return false;
                            }
                        }
                    }
                    return bookpage.doTouchEvent(e);
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReadSettingDialog.isShow()){
            mReadSettingDialog.dismiss();
            mReadSettingDialog = null;
        }
        pageFactory.setPageWidget(null);
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
