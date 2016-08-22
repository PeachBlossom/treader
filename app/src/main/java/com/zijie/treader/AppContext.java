package com.zijie.treader;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.zijie.treader.db.BookList;
import com.zijie.treader.util.PageFactory;
import com.zijie.treader.util.PageFactory1;

import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Administrator on 2016/7/8 0008.
 */
public class AppContext extends LitePalApplication {
    public static volatile Context applicationContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

        LitePalApplication.initialize(this);
        Config.createConfig(this);
        PageFactory1.createPageFactory(this);
    }

}
