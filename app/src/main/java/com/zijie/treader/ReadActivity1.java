package com.zijie.treader;

import android.os.Bundle;
import android.view.WindowManager;

import com.zijie.treader.base.BaseActivity;
import com.zijie.treader.view.BookPageWidget;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2016/7/15 0015.
 */
public class ReadActivity1 extends BaseActivity {

    @Bind(R.id.bookpage)
    BookPageWidget bookpage;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_read1;
    }

    @Override
    protected void initData() {
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void initListener() {

    }

}
